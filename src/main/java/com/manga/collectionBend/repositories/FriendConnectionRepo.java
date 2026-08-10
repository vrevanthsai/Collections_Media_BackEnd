package com.manga.collectionBend.repositories;

import com.manga.collectionBend.entities.FriendConnection;
import com.manga.collectionBend.utils.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendConnectionRepo extends JpaRepository<FriendConnection, Integer> {

    // ---- Check if ANY connection exists between two users, regardless of direction/status ----
    @Query("""
        SELECT CASE WHEN COUNT(fc) > 0 THEN true ELSE false END
        FROM FriendConnection fc
        WHERE (fc.requester.userId = :userA AND fc.receiver.userId = :userB)
           OR (fc.requester.userId = :userB AND fc.receiver.userId = :userA)
        """)
    boolean existsBetween(@Param("userA") Integer userA, @Param("userB") Integer userB);


    // ---- Find the connection row between two users, regardless of direction (any status) ----
    @Query("""
        SELECT fc FROM FriendConnection fc
        WHERE (fc.requester.userId = :userA AND fc.receiver.userId = :userB)
           OR (fc.requester.userId = :userB AND fc.receiver.userId = :userA)
        """)
    Optional<FriendConnection> findBetween(@Param("userA") Integer userA, @Param("userB") Integer userB);


    // ---- Find an ACCEPTED (friends) connection between two specific users ----
    @Query("""
        SELECT fc FROM FriendConnection fc
        WHERE fc.status = 'ACCEPTED'
          AND ((fc.requester.userId = :userA AND fc.receiver.userId = :userB)
           OR (fc.requester.userId = :userB AND fc.receiver.userId = :userA))
        """)
    Optional<FriendConnection> findAcceptedBetween(@Param("userA") Integer userA, @Param("userB") Integer userB);


    // ---- Find a REJECTED connection between two users (for cooldown check) ----
    @Query("""
        SELECT fc FROM FriendConnection fc
        WHERE fc.status = 'REJECTED'
          AND ((fc.requester.userId = :userA AND fc.receiver.userId = :userB)
           OR (fc.requester.userId = :userB AND fc.receiver.userId = :userA))
        """)
    Optional<FriendConnection> findRejectedBetween(@Param("userA") Integer userA, @Param("userB") Integer userB);


    // ---- All PENDING requests RECEIVED by a user (for notification/requests list) ----
    // this one is a simple derived query — no custom JPQL needed
    List<FriendConnection> findByReceiver_UserIdAndStatus(Integer receiverId, FriendStatus status);


    // ---- All PENDING requests SENT by a user (to show "requests you've sent, awaiting response") ----
    List<FriendConnection> findByRequester_UserIdAndStatus(Integer requesterId, FriendStatus status);


    // ---- All ACCEPTED connections involving a user, from EITHER side (for friends list) ----
    @Query("""
        SELECT fc FROM FriendConnection fc
        WHERE fc.status = 'ACCEPTED'
          AND (fc.requester.userId = :userId OR fc.receiver.userId = :userId)
        """)
    List<FriendConnection> findAcceptedByEitherUser(@Param("userId") Integer userId);


    // ---- Check if a specific user has BLOCKED another (directional — matters who blocked whom) ----
    @Query("""
    SELECT CASE WHEN COUNT(fc) > 0 THEN true ELSE false END
    FROM FriendConnection fc
    WHERE fc.status = 'BLOCKED'
      AND fc.requester.userId = :blockerId
      AND fc.receiver.userId = :blockedId
    """)
    boolean isBlockedBy(@Param("blockerId") Integer blockerId, @Param("blockedId") Integer blockedId);

    @Query("""
    SELECT fc FROM FriendConnection fc
    WHERE fc.status = 'BLOCKED' AND fc.requester.userId = :blockerId
    """)
    List<FriendConnection> findBlockedByUser(@Param("blockerId") Integer blockerId);
}
