package com.manga.collectionBend.repositories;

import com.manga.collectionBend.entities.CollectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionRepo extends JpaRepository<CollectionEntity,Integer> {
}
