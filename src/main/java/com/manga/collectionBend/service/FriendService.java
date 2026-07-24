package com.manga.collectionBend.service;

import com.manga.collectionBend.auth.repositories.UserRepo;
import com.manga.collectionBend.dto.ApiResponse;
import com.manga.collectionBend.dto.FriendDto;
import com.manga.collectionBend.entities.FriendConnection;
import com.manga.collectionBend.repositories.FriendConnectionRepo;
import com.manga.collectionBend.utils.FriendStatus;
import com.manga.collectionBend.utils.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendConnectionRepo friendConnectionRepo;
    private final UserRepo userRepo;
    private final NotificationService notificationService;

    public ApiResponse<String> sendFriendRequest(Integer requesterId, Integer receiverId) {
        if (requesterId.equals(receiverId)) {
//            throw new IllegalArgumentException("You cannot send a friend request to yourself");
            return ApiResponse.error("You cannot send a friend request to yourself");
        }
        if (friendConnectionRepo.isBlockedBy(receiverId, requesterId)) {
//            throw new IllegalStateException("You cannot send a request to this user");
            return ApiResponse.error("You cannot send a request to this user");
        }

        var rejected = friendConnectionRepo.findRejectedBetween(requesterId, receiverId);
        if (rejected.isPresent()) {
            LocalDateTime cooldownEnd = rejected.get().getRespondedAt().plusWeeks(1);
            if (LocalDateTime.now().isBefore(cooldownEnd)) {
//                throw new IllegalStateException("You must wait before re-sending a request to this user");
                return ApiResponse.error("You must wait before re-sending a request to this user");
            }
            friendConnectionRepo.delete(rejected.get());
        }

        if (friendConnectionRepo.existsBetween(requesterId, receiverId)) {
//            throw new IllegalStateException("A friend connection already exists between these users");
            return ApiResponse.error("A friend connection already exists between these users");
        }

        var requester = userRepo.findById(requesterId).orElseThrow();
        var receiver = userRepo.findById(receiverId).orElseThrow();

        FriendConnection connection = new FriendConnection();
        connection.setRequester(requester);
        connection.setReceiver(receiver);
        connection.setStatus(FriendStatus.PENDING);
        connection.setCreatedAt(LocalDateTime.now());
        friendConnectionRepo.save(connection);

        notificationService.createNotification(receiver, requester, NotificationType.FRIEND_REQUEST, connection.getId());
        return ApiResponse.success("Friend request sent");
    }

    public void respondToRequest(Integer connectionId, FriendStatus newStatus, Integer currentId) {
        var connection = friendConnectionRepo.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        //        Here in this Api service - only connection receiver should only respond-Accept or Reject
//        so we prevent requester responding
        if(Objects.equals(currentId, connection.getReceiver().getUserId())){
            connection.setStatus(newStatus);
            connection.setRespondedAt(LocalDateTime.now());
            friendConnectionRepo.save(connection);

            // remove the original pending request notification, action is done
            notificationService.removeNotificationByReference(connectionId, NotificationType.FRIEND_REQUEST);

            if (newStatus == FriendStatus.ACCEPTED) {
                notificationService.createNotification(
                        connection.getRequester(), connection.getReceiver(),
                        NotificationType.FRIEND_ACCEPTED, connection.getId());
            }
        } else {
            throw new IllegalStateException("The provided userId: "+ currentId +" is not a receiverId, so responding to this Connection Request is not possible, only receiver can respond");
        }
    }

    public List<FriendDto> getFriends(Integer userId) {
        return friendConnectionRepo.findAcceptedByEitherUser(userId).stream()
                .map(c -> c.getRequester().getUserId().equals(userId) ? c.getReceiver() : c.getRequester())
                .map(FriendDto::fromEntity) // we use direct build object from DTO method and map it to list to send to frontend
                .toList();
    }

    public void unfriend(Integer myUserId, Integer otherUserId) {
        var connection = friendConnectionRepo.findAcceptedBetween(myUserId, otherUserId)
                .orElseThrow(() -> new RuntimeException("Not friends"));

        Integer connectionId = connection.getId();
        friendConnectionRepo.delete(connection);

        // also clean up any lingering notifications tied to this connection
        notificationService.removeAllNotificationsByReference(connectionId);
    }

    public void blockUser(Integer blockerId, Integer blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException("You cannot block yourself");
        }

        var existing = friendConnectionRepo.findBetween(blockerId, blockedId);

        if (existing.isPresent()) {
            FriendConnection connection = existing.get();
            // regardless of prior status (PENDING, ACCEPTED, REJECTED) — blocking overrides it
            connection.setRequester(userRepo.getReferenceById(blockerId)); // re-anchor: blocker is now "requester" of the block action
            connection.setReceiver(userRepo.getReferenceById(blockedId));
            connection.setStatus(FriendStatus.BLOCKED);
            connection.setRespondedAt(LocalDateTime.now());
            friendConnectionRepo.save(connection);
        } else {
            var blocker = userRepo.findById(blockerId).orElseThrow();
            var blocked = userRepo.findById(blockedId).orElseThrow();

            FriendConnection block = new FriendConnection();
            block.setRequester(blocker); // "requester" here means "who initiated the block"
            block.setReceiver(blocked);
            block.setStatus(FriendStatus.BLOCKED);
            block.setCreatedAt(LocalDateTime.now());
            block.setRespondedAt(LocalDateTime.now());
            friendConnectionRepo.save(block);
        }
    }

    public void unblockUser(Integer blockerId, Integer blockedId) {
        var connection = friendConnectionRepo.findBetween(blockerId, blockedId)
                .filter(c -> c.getStatus() == FriendStatus.BLOCKED)
                .orElseThrow(() -> new IllegalStateException("No active block found between these users"));

        // ensure only the person who INITIATED the block can unblock (not the blocked person)
        if (!connection.getRequester().getUserId().equals(blockerId)) {
            throw new IllegalStateException("You are not authorized to unblock this user");
        }

        Integer connectionId = connection.getId();
        friendConnectionRepo.delete(connection); // fully remove — back to "no connection" (strangers)
        // also clean up any lingering notifications tied to this connection
        notificationService.removeAllNotificationsByReference(connectionId);
    }

    public List<FriendDto> getBlockedUsers(Integer blockerId) {
        return friendConnectionRepo.findBlockedByUser(blockerId).stream()
                .map(c -> FriendDto.fromEntity(c.getReceiver())) // receiver = the one who got blocked
                .toList();
    }
}
