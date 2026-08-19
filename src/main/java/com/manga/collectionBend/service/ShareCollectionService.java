package com.manga.collectionBend.service;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.repositories.UserRepo;
import com.manga.collectionBend.dto.GroupedShareDto;
import com.manga.collectionBend.dto.ShareResultDto;
import com.manga.collectionBend.dto.SharedCollectionDto;
import com.manga.collectionBend.entities.CollectionEntity;
import com.manga.collectionBend.entities.SharedCollection;
import com.manga.collectionBend.repositories.CollectionRepo;
import com.manga.collectionBend.repositories.FriendConnectionRepo;
import com.manga.collectionBend.repositories.SharedCollectionRepo;
import com.manga.collectionBend.utils.NotificationType;
import com.manga.collectionBend.utils.ShareActionStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShareCollectionService {
    private static final int MAX_SHARES_PER_WINDOW = 5;         // max collections one friend can receive per window
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofHours(2); // rolling time window for the limit

    private final SharedCollectionRepo sharedCollectionRepo;
    private final CollectionRepo collectionRepo;
    private final FriendConnectionRepo friendConnectionRepo;
    private final UserRepo userRepo;
    private final NotificationService notificationService;

    @Transactional
    public ShareResultDto shareCollections(List<Integer> collectionIds, Integer sharerId, List<Integer> friendUserIds) {

        // rate-limit window start — only shares sent within the last 2 hrs count toward the limit
        LocalDateTime windowStart = LocalDateTime.now().minus(RATE_LIMIT_WINDOW);

        // tracks friends who were skipped entirely or only partially fulfilled, for the response summary
        List<String> skippedRecipients = new ArrayList<>();
        int totalSharesCreated = 0;

        // fetch all requested collections in one query
        List<CollectionEntity> collections = collectionRepo.findAllById(collectionIds);

        // ownership check — sharer must own every collection they're trying to share
        for (CollectionEntity collection : collections) {
            UserEntity owner = collection.getCategory().getUser();
            if (!owner.getUserId().equals(sharerId)) {
                throw new IllegalStateException("You can only share your own collections");
            }
        }

        // lightweight reference (no extra SELECT) — used as the FK owner on each SharedCollection row
        var sharerRef = userRepo.getReferenceById(sharerId);

        // process each selected friend independently, so one friend's limit doesn't block others
//        limit - a user can only share 5 collections to his friends per 2 hrs and TODO- only for 10 friends in 2 hrs
        for (Integer friendId : friendUserIds) {

            // only share with users who are actually accepted friends
            boolean areFriends = friendConnectionRepo.findAcceptedBetween(sharerId, friendId).isPresent();
            if (!areFriends) {
                continue; // not a friend — skip silently or TODO- THROW error or send error msg to frontend that this user is not friend
            }

            // count how many collections were already shared to THIS friend within the rate-limit window
            long recentCount = sharedCollectionRepo.countRecentSharesToUser(sharerId, friendId, windowStart);
            long remainingQuota = MAX_SHARES_PER_WINDOW - recentCount;

            if (remainingQuota <= 0) {
                // this friend has no quota left at all — skip them entirely- his limit of 5 shares completed
                var friendEntity = userRepo.findById(friendId).orElseThrow();
                skippedRecipients.add(friendEntity.getUniqueUsername()); // use these usernames in frontend to show this friends share limit is done , so share after 2 hrs
                continue; // skip this friend iteration and go to next
            }

            var friendRef = userRepo.getReferenceById(friendId);

            // only take as many collections as this friend's remaining quota allows (partial fill)
//            for example - Positive case- if A user shares 2 collections to B and B has 3 remaining quotes/slots(remaining 2 already shared by A to B) within 2 hrs
//            - then 2 collections are shared to B and B has 1 remaining slot
//            Negative case- if A user shares 4 collections to B and B has only 3 remaining quotes/slots(remaining 2 already shared by A to B) within 2 hrs
//            - then only first 3 collections from share list of A will be sent to B because B only has 3 slots and last unshared collection of A share list will not be considered and B reaches 5 max-slot size for that 2 hrs
//            final case - after 2 hrs again freshly- A can share upto 5 collections to B (B's limit is reset) and after same process continues
            List<CollectionEntity> collectionsToShare = collections.size() <= remainingQuota
                    ? collections
                    : collections.subList(0, (int) remainingQuota);

            Integer lastShareId = null; // used to anchor the grouped notification to the latest share row

//            looping the final shareCollection list and separately storing each share collection item as single row in ShareCollection table
            for (CollectionEntity collection : collectionsToShare) {
                // build one SharedCollection row per collection, per friend
                SharedCollection share = SharedCollection.builder()
                        .collection(collection)
                        .sharedBy(sharerRef)
                        .sharedWith(friendRef)
                        .sharedAt(LocalDateTime.now())
                        .isViewed(false)                       // recipient hasn't seen it yet
                        .actionStatus(ShareActionStatus.PENDING) // recipient hasn't acted on it yet
                        .build();

                sharedCollectionRepo.save(share);
                lastShareId = share.getId();
                totalSharesCreated++;
            }

            // one grouped notification per friend (not per collection) to avoid notification spam
//            instead of showing each notification per share collection item among total shares(5)
//            - we only send last share collection id to frontend in single notification for receiver friends account - which avoids spam
            notificationService.createNotification(
                    friendRef, sharerRef, NotificationType.COLLECTION_SHARED, lastShareId);

            // flag if this friend only received a subset due to hitting their quota mid-way
//            this skippedRecipients- are friends of A user where their total share collections list is trimmed/subset and send from A to these friends
            if (collectionsToShare.size() < collections.size()) {
                var friendEntity = userRepo.findById(friendId).orElseThrow();
                skippedRecipients.add(friendEntity.getUniqueUsername() + " (partial — quota reached)");
            }
        }

        // summary returned to frontend: how many shares actually went through + who was skipped/partial
//        finally we use this 2 data and send a popup to A user - which friends received total shares and which friends got trimmed shares due to limit within 2 hrs
        return ShareResultDto.builder()
                .totalSharesCreated(totalSharesCreated)
                .skippedOrPartialRecipients(skippedRecipients)
                .build();
    }

//    returns list of share collections done from friends(multiple) to one user
    public List<SharedCollectionDto> getSharedWithMe(Integer userId) {
        return sharedCollectionRepo.findBySharedWith_UserIdOrderBySharedAtDesc(userId).stream()
                .map(SharedCollectionDto::fromEntity)
                .toList();
    }

    public void markAsViewed(Integer shareId) {
        var share = sharedCollectionRepo.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Share not found"));
        share.setViewed(true);
        sharedCollectionRepo.save(share);
    }

// TODO-  update this method logic - either to record how other user is adding the shared collection of A user into their collections data/list/account(ex- check both user's collection names)
//    - or remove this actionStatus logic and add logic to record/store Liked feature same like sharing posts and getting likes or reactions in Instagram
    public void updateActionStatus(Integer shareId, ShareActionStatus status) {
        var share = sharedCollectionRepo.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Share not found"));
        share.setActionStatus(status);
        sharedCollectionRepo.save(share);
    }

//    this method sends organized shared collections list with their friends details who shared them
//    this is used for Shared Collections page in frontend - where each row is about friend user details at top and below contains cards of shared collections data and in sequence all rows are displayed here in this page
    public List<GroupedShareDto> getGroupedSharesFromFriends(Integer userId) {
        // fetch all shares received by this user, most recent first
        List<SharedCollection> shares = sharedCollectionRepo.findBySharedWith_UserIdOrderBySharedAtDesc(userId);

        // group shares by who sent them, preserving insertion order (most recent sharer group first)
//        data is a Map(key/value pair) - of kay = userId(of one friend) and value = shared collections list by that user(upto 5 max per 2 hrs)
        Map<Integer, List<SharedCollection>> grouped = shares.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getSharedBy().getUserId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return grouped.values().stream()
                .map(group -> {
                    UserEntity sharer = group.get(0).getSharedBy(); // same sharer across the whole group

                    return GroupedShareDto.builder()
                            .sharedByUserId(sharer.getUserId())
                            .sharedByUsername(sharer.getUniqueUsername())
                            .sharedByImageName(sharer.getImageName())
                            .collectionCount(group.size())
                            .latestSharedAt(group.get(0).getSharedAt()) // list is already ordered desc, so first = latest
                            .collections(group.stream().map(SharedCollectionDto::fromEntity).toList())
                            .build();
                })
                .toList();
    }
}
