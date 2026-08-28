package com.manga.collectionBend.repositories;

import com.manga.collectionBend.entities.SharedCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SharedCollectionRepo extends JpaRepository<SharedCollection, Integer> {

//  findBySharedWith_UserId- finds list - in Sharewith column of ShareCollection and goes inside userEntity matching its userId
//    OrderBySharedAtDesc- sorts the obtained list in Descending order based on SharedAt field(date/time) stored for each ShareCollection item of list
    List<SharedCollection> findBySharedWith_UserIdOrderBySharedAtDesc(Integer userId);

    List<SharedCollection> findBySharedBy_UserIdOrderBySharedAtDesc(Integer userId);

//    if will find currentUserId in SharedWith column of table and checks for isAddedToWatchlist columns which are marked as true(1) value in DB and order them in Desc order(latest first) and returns all in list
    List<SharedCollection> findBySharedWith_UserIdAndIsAddedToWatchlistTrueOrderBySharedAtDesc(Integer userId);

    List<SharedCollection> findBySharedWith_UserIdAndIsViewedFalse(Integer userId);

    long countBySharedWith_UserIdAndIsViewedFalse(Integer userId);

    @Query("""
    SELECT COUNT(s) FROM SharedCollection s
    WHERE s.sharedBy.userId = :sharerId
      AND s.sharedWith.userId = :recipientId
      AND s.sharedAt >= :windowStart
    """)
    long countRecentSharesToUser(
            @Param("sharerId") Integer sharerId,
            @Param("recipientId") Integer recipientId,
            @Param("windowStart") LocalDateTime windowStart
    );

//    Check if there is a shared-collection record from one user(shareBY) to another user(shareWith)
    @Query("""
        SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END
        FROM SharedCollection sc
        WHERE (sc.sharedBy.userId = :userA AND sc.sharedWith.userId = :userB)
        """)
    boolean existsBetween(@Param("userA") Integer userA, @Param("userB") Integer userB);

//    Checks if a collectionId already shared with some-user-friendId(currentUserId not required here, because collectionId are unique)
    boolean existsByCollection_CollectionIdAndSharedWith_UserId(Integer collectionId, Integer sharedWithUserId);
}