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
import com.manga.collectionBend.utils.RecommendationsTabType;
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
    private static final int MAX_SHARES_PER_WINDOW = 2;         // max collections one friend can receive per window
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofHours(1); // rolling time window for the limit

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
        // tracks duplicate-share skips - this new array divides collections into alreadySharedMsg and newCollections to avoid duplicate entries
        List<String> alreadySharedMessages = new ArrayList<>();
        int totalSharesCreated = 0;

        // fetch all requested collections in one query
        List<CollectionEntity> collections = collectionRepo.findAllById(collectionIds);

        if(!collections.isEmpty()) {
            // ownership check — sharer must own every collection they're trying to share
            for (CollectionEntity collection : collections) {
                UserEntity owner = collection.getCategory().getUser();
                if (!owner.getUserId().equals(sharerId)) {
                    throw new IllegalStateException("You can only share your own collections");
                }
            }
        } else {
            throw new IllegalStateException("Empty collections list can not be shared");
        }

        if(friendUserIds.isEmpty()) {
            throw new IllegalStateException("friend userIds list can not be empty to share collections");
        }

        // lightweight reference (no extra SELECT) — used as the FK owner on each SharedCollection row
        var sharerRef = userRepo.getReferenceById(sharerId);

        // process each selected friend independently, so one friend's limit doesn't block others
//        limit - a user can only share 5 collections to his friends per 2 hrs and TODO- only for 10 friends in 2 hrs
        for (Integer friendId : friendUserIds) {

            // only share with users who are actually accepted friends
            boolean areFriends = friendConnectionRepo.findAcceptedBetween(sharerId, friendId).isPresent();
            if (!areFriends) {
                throw new IllegalStateException("You can not share collections to Users - who are not your friends!!");
//                continue; // not a friend — skip silently or TODO- THROW error or send error msg to frontend that this user is not friend
            }

            // count how many collections were already shared to THIS friend within the rate-limit window
            long recentCount = sharedCollectionRepo.countRecentSharesToUser(sharerId, friendId, windowStart);
            long remainingQuota = MAX_SHARES_PER_WINDOW - recentCount;

            if (remainingQuota <= 0) {
                // this friend has no quota left at all — skip them entirely - his limit of 5 shares completed
                var friendEntity = userRepo.findById(friendId).orElseThrow();
//                if already shared collection is given then also, it will only skip this friend iteration and go to next friend iteration instead of adding this into alreadySharedMsg array due to limit reached
                skippedRecipients.add(friendEntity.getUniqueUsername() + " ,Reason:- this friend share limit reached max"); // use these usernames in frontend to show this friends share limit is done , so share after 2 hrs
                continue; // skip this friend iteration and go to next
            }

            var friendRef = userRepo.getReferenceById(friendId);
            var friendEntity = userRepo.findById(friendId).orElseThrow();

//            duplication check/validation logic
            // filter out collections already shared with this specific friend- to prevent duplicate entries into SharedCollections table which already has shared collection with a user-friend
            List<CollectionEntity> newCollectionsOnly = new ArrayList<>();
            for (CollectionEntity collection : collections) {
                boolean alreadyShared = sharedCollectionRepo
                        .existsByCollection_CollectionIdAndSharedWith_UserId(collection.getCollectionId(), friendId);

                if (alreadyShared) {
                    // Here if userA already shared collection-101 with userB then - we add its msg and below in else block - newCollectionsOnly array will be empty
                    // - then in line 117 if() block - this friend-userB iteration will be skipped - so that below 117 lines- save sharedCollection logic will not run and new iteration of next userC-friend will start
//                    this will prevent duplicate entries and also send custom msg to frontend
                    alreadySharedMessages.add(
                            "\"" + collection.getName() + "\" collection was already shared with " + friendEntity.getUniqueUsername() +" friend");
                } else {
//                    if this collection-101 is not shared from userA to userB then it will be added to this newCollectionsOnly var then in line 116 if() will not run and continue with save sharedCollection logic because its a new entry with is not shared yet
                    newCollectionsOnly.add(collection);
                }
            }

            if (newCollectionsOnly.isEmpty()) {
//                this skips this iteration of a friend and does not run below save logic and goes to next user-friendId and undergoes same duplication check/validation logic
                continue; // nothing new to share with this friend, skip entirely
            }

            // only take as many collections as this friend's remaining quota allows (partial fill)
//            for example - Positive case- if A user shares 2 collections to B and B has 3 remaining quotes/slots(remaining 2 already shared by A to B) within 2 hrs
//            - then 2 collections are shared to B and B has 1 remaining slot
//            Negative case- if A user shares 4 collections to B and B has only 3 remaining quotes/slots(remaining 2 already shared by A to B) within 2 hrs
//            - then only first 3 collections from share list of A will be sent to B because B only has 3 slots and last unshared collection of A share list will not be considered and B reaches 5 max-slot size for that 2 hrs
//            final case - after 2 hrs again freshly- A can share upto 5 collections to B (B's limit is reset) and after same process continues
            //  apply quota on top of the FILTERED list — must use newCollectionsOnly
            List<CollectionEntity> collectionsToShare = newCollectionsOnly.size() <= remainingQuota
                    ? newCollectionsOnly
                    : newCollectionsOnly.subList(0, (int) remainingQuota);

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
//            also stores total shares count done by A user to B friend user in notification row table
            notificationService.createNotification(
                    friendRef, sharerRef, NotificationType.COLLECTION_SHARED, lastShareId, collectionsToShare.size());

            // flag if this friend only received a subset due to hitting their quota mid-way
//            this skippedRecipients- are friends of A user where their total share collections list is trimmed/subset and send from A to these friends
            if (collectionsToShare.size() < collections.size()) {
                skippedRecipients.add(friendEntity.getUniqueUsername() + " Reason:- (partial — quota reached or collection already shared)");
            }
        }

        // summary returned to frontend: how many shares actually went through + who was skipped/partial
//        finally we use this 2 data and send a popup to A user - which friends received total shares and which friends got trimmed shares due to limit within 2 hrs
        return ShareResultDto.builder()
                .totalSharesCreated(totalSharesCreated)
                .skippedOrPartialRecipients(skippedRecipients)
                .alreadySharedMessages(alreadySharedMessages)
                .build();
    }

//    returns list of share collections done by one user to his friends(multiple)
    public List<SharedCollectionDto> getSharedByMe(Integer userId) {
        return sharedCollectionRepo.findBySharedBy_UserIdOrderBySharedAtDesc(userId).stream()
                .map(SharedCollectionDto::fromEntity)
                .toList();
    }

    public void markAsViewed(Integer shareId, Integer userId) {
        var share = sharedCollectionRepo.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Share not found"));
        if(share.getSharedWith().getUserId().equals(userId)) {
            share.setViewed(true);
            sharedCollectionRepo.save(share);
        } else {
            throw new IllegalStateException("You can not mark this shared collection as viewed- only Receiver user must mark as viewed!");
        }
    }

//  This method changes the actionStatus of shared collection only done by receiver user(sharedWith)
    public void updateActionStatus(Integer shareId, ShareActionStatus status, Integer userId) {
        if(status == null){
            throw new IllegalStateException("status can not be null");
        }
        var share = sharedCollectionRepo.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Share not found"));
        if(share.getSharedWith().getUserId().equals(userId)) {
            share.setActionStatus(status);
            sharedCollectionRepo.save(share);
            if(status == ShareActionStatus.LIKED) {
//                here after shareWith/receiver acts or changes actionStatus to LIKED then we send notification to shareBy/sender friend-user to let him know that his suggested/shared collection was liked by his friend
//                one shareId record = one collectionId record - so we can call first shareId - next inside its stored collectionId
                notificationService.createNotification(
                        share.getSharedBy(), share.getSharedWith(), NotificationType.COLLECTION_LIKED, shareId, share.getCollection().getCollectionId(), share.getCollection().getName());
            }
            else if(status == ShareActionStatus.PENDING){
//                delete any notification records when Status is PENDING(either initial value or unliking toggle value) which may be created for this ShareId- when its status was LIKED previously
                notificationService.removeNotificationByReferenceIdForCollectionLikedType(shareId);
            }
        } else {
            throw new IllegalStateException("You can not change actionStatus for this shared collection- only Receiver user can change action status!");
        }
    }

    //    returns list of share collections done from friends(multiple) to one user
//    this method sends organized shared collections list with their friends details who shared them
//    this is used for Shared Collections page in frontend - where each row is about friend user details at top and below contains cards of shared collections data and in sequence all rows are displayed here in this page
//    this method sends groupedSharedCollections data along with it User details for Both cases/tabs like 1) Share with Me and 2)Share by Me
    public List<GroupedShareDto> getGroupedSharesFromFriends(Integer userId, RecommendationsTabType tabType) {
        List<SharedCollection> shares;
        if(tabType == RecommendationsTabType.SHARE_WITH_ME){
            // fetch all shares received by this user, most recent first
            shares = sharedCollectionRepo.findBySharedWith_UserIdOrderBySharedAtDesc(userId);
        } else if(tabType == RecommendationsTabType.SHARE_BY_ME) {
//            fetch all shares sent by this user to his friends
            shares = sharedCollectionRepo.findBySharedBy_UserIdOrderBySharedAtDesc(userId);
        } else if(tabType == RecommendationsTabType.MY_WATCH_LIST) {
//            fetch all shares which are marked as WatchList(true) by SharedWith user(receiver)
            shares = sharedCollectionRepo.findBySharedWith_UserIdAndIsAddedToWatchlistTrueOrderBySharedAtDesc(userId);
        } else {
            return null;
        }

        // group shares by who sent them, preserving insertion order (most recent sharer group first)
//        data is a Map(key/value pair) - of kay = userId(of one friend) and value = shared collections list by that user(upto 5 max per 2 hrs)
//        Collectors.toList() - This is the downstream collector — it tells groupingBy how to collect the values within each group. Here, it says: "for each key (sharer), collect all matching SharedCollection items into a List." This is what produces the List<SharedCollection> as the map's value type.
//        here- this map- collects all shared collections which have same shareBy.userId and put then in single list and use this list as value of a Map-pair and add this list-value to sharedBy.userId(integer) as Key of same pair- instead of creating multiple pairs with same key/shardBy.userId
//        this returns array-objects as json- where single object has single user details and combined shared collections details over time(till now- not based on hrs)
        Map<Integer, List<SharedCollection>> grouped = shares.stream()
                .collect(Collectors.groupingBy(
                        //  pick the correct grouping key based on which "side" of the share we're viewing
                        s -> tabType == RecommendationsTabType.SHARE_BY_ME
                                ? s.getSharedWith().getUserId()  // group by RECIPIENT when viewing "shared by me"
                                : s.getSharedBy().getUserId(),   // group by SENDER when viewing "shared with me" / watchlist
                        LinkedHashMap::new,                 // preserve insertion order in the result map
                        Collectors.toList()                 // collect each group's items into a List
                ));

        return grouped.values().stream()
                .map(group -> {
                    UserEntity sharer;
//                    Sharer/User details
                    if(tabType == RecommendationsTabType.SHARE_WITH_ME || tabType == RecommendationsTabType.MY_WATCH_LIST) {
//                        sharer = who shared/recommended collections with/to me
                        sharer = group.get(0).getSharedBy(); // same sharer across the whole group
                    } else {
//                        sharer = whom i shared/recommended collection to
                        sharer = group.get(0).getSharedWith();
                    }

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

    public void markAsWatchList(Integer shareId, Boolean isWatchList, Integer userId) {
        var share = sharedCollectionRepo.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Share not found"));
        if(share.getSharedWith().getUserId().equals(userId)) {
//            logic used for toggling in Frontend Watch button- where user can add or remove a recommended collections from his WatchList tab/page
            if(isWatchList){
                share.setAddedToWatchlist(true);
                sharedCollectionRepo.save(share);
            } else {
                share.setAddedToWatchlist(false);
                sharedCollectionRepo.save(share);
            }
        } else {
            throw new IllegalStateException("You can not update this shared collection watchlist status- only Receiver user must update!");
        }
    }
}
