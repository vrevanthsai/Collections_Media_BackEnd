package com.manga.collectionBend.repositories;

import com.manga.collectionBend.entities.NotificationEntity;
import com.manga.collectionBend.utils.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepo extends JpaRepository<NotificationEntity, Integer> {

    List<NotificationEntity> findByRecipient_UserIdOrderByCreatedAtDesc(Integer recipientId);

    List<NotificationEntity> findByRecipient_UserIdAndIsReadFalseOrderByCreatedAtDesc(Integer recipientId);

    long countByRecipient_UserIdAndIsReadFalse(Integer recipientId);

    // used when a friend request is accepted/rejected — remove the original "FRIEND_REQUEST" notification
    // so it doesn't keep showing as pending after it's been actioned
//    here referenceId is either FriendConnectionId or Collection_shared_Id
    Optional<NotificationEntity> findByReferenceIdAndType(Integer referenceId, NotificationType type);

//    only removes rows linked to Friend_ConnectionId only between 2 users , not their collection_Id of Type Collection_Shared
    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.referenceId = :referenceId AND n.type IN ('FRIEND_REQUEST', 'FRIEND_ACCEPTED')")
    void deleteAllByReferenceIdForFriendTypes(@Param("referenceId") Integer referenceId);
}
