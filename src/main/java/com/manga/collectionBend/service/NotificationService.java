package com.manga.collectionBend.service;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.dto.NotificationDto;
import com.manga.collectionBend.entities.NotificationEntity;
import com.manga.collectionBend.repositories.NotificationRepo;
import com.manga.collectionBend.utils.NotificationType;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class NotificationService {

    private final NotificationRepo notificationRepo;

    public NotificationService(NotificationRepo notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    public void createNotification(UserEntity recipient, UserEntity actor, NotificationType type, Integer referenceId) {
        NotificationEntity notification = NotificationEntity.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .referenceId(referenceId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepo.save(notification);
    }

    public List<NotificationDto> getAllNotifications(Integer userId) {
        return notificationRepo.findByRecipient_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationDto::fromEntity)
                .toList();
    }

    public List<NotificationDto> getUnreadNotifications(Integer userId) {
        return notificationRepo.findByRecipient_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(NotificationDto::fromEntity)
                .toList();
    }

    public long getUnreadCount(Integer userId) {
        return notificationRepo.countByRecipient_UserIdAndIsReadFalse(userId);
    }

    public void markAsRead(Integer notificationId) {
        var notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepo.save(notification);
    }

    public void markAllAsRead(Integer userId) {
        List<NotificationEntity> unread = notificationRepo.findByRecipient_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepo.saveAll(unread);
    }

    // removes the original FRIEND_REQUEST notification once it's been accepted/rejected,
    // so it doesn't linger in the list as "pending" after being actioned
    public void removeNotificationByReference(Integer referenceId, NotificationType type) {
        notificationRepo.findByReferenceIdAndType(referenceId, type)
                .ifPresent(notificationRepo::delete);
    }

//    removes all notification rows which are linked/referred to referenceId which as (friend)connectionId which is removed/deleted in FriendConnections table when unFriend api between 2 users is called
    @Transactional
    public void removeAllNotificationsByReference(Integer referenceId) {
        notificationRepo.deleteAllByReferenceIdForFriendTypes(referenceId);
    }

    public void deleteNotificationHandler(Integer notificationId, Integer userId) {
        var notification =  notificationRepo.findById(notificationId).orElseThrow(() -> new RuntimeException("Notification not found"));
//        we only delete notification record if provided userId is same as recipient(userId- who received notification) is present in the existing notification record
        if(Objects.equals(userId, notification.getRecipient().getUserId())) {
            notificationRepo.delete(notification);
        } else {
            throw new IllegalStateException("your userId: "+ userId +" is not matching recipientId, so deleting this notification is not possible");
        }
    }
}
