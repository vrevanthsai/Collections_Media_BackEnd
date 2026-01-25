package com.manga.collectionBend.auth.repositories;

import com.manga.collectionBend.auth.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<UserEntity, Integer> {

//    JpaRepository class does not have specific field finding methods-so we create them
//    findByUsername- this is the convention of JPA to write custom Repo method to automatically generate its own query to find/fetch our specific field from DB
//    naming formate- findBy+CapitalFirstLetter of Field/Column name(eg-Username)
    Optional<UserEntity> findByUsername(String username);
}
