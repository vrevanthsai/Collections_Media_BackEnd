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
}