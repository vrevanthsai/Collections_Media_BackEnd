package com.manga.collectionBend.auth.repositories;

import com.manga.collectionBend.auth.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Integer> {
//    custom method to fetch RefreshToken field data from its Table
//    JpaRepository class does not have specific field finding methods-so we create them
//    findByRefreshToken- this is the convention of JPA to write custom Repo method to automatically generate its own query to find/fetch our specific field from DB
//    naming formate- findBy+CapitalFirstLetter of Field/Column name
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
