package com.manga.collectionBend.repositories;

import com.manga.collectionBend.entities.CollectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepo extends JpaRepository<CollectionEntity,Integer> {
//    Custom Method to get(GET) User Based Collections Data
//    naming formate- findBy+CapitalFirstLetter of Field/Column name(eg-Username) or you can create a sql query to fetch data
    List<CollectionEntity> findByUserId(String userId);
}
