package com.manga.collectionBend.auth.services;

import com.manga.collectionBend.auth.entities.RefreshToken;
import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.repositories.RefreshTokenRepo;
import com.manga.collectionBend.auth.repositories.UserRepo;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

// This file generates RefreshTokens
@Service
public class RefreshTokenService {

    private final UserRepo userRepo;

    private final RefreshTokenRepo refreshTokenRepo;

    public RefreshTokenService(UserRepo userRepo, RefreshTokenRepo refreshTokenRepo) {
        this.userRepo = userRepo;
        this.refreshTokenRepo = refreshTokenRepo;
    }

//  Generating Refresh Token Method
    public RefreshToken createRefreshToken(String username){
        UserEntity user = userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("user not found with email: " + username));

//        getting RefreshToken data from user table
        RefreshToken refreshToken = user.getRefreshToken();

//        if refreshToken is empty then generate a new one
        if(refreshToken == null){
//            long refreshTokenValidity = 5*60*60*10000; // (formate-hrs*mins*sec*millisec) equal to 5 hours
            long refreshTokenValidity = 30 * 1000; // =30Sec for testing API purpose

//            this builder should have all vars of RefreshToken class
            refreshToken = RefreshToken.builder()
                    .refreshToken(UUID.randomUUID().toString()) // this generates Refresh token with help of UUID pattern
//          expirationTime of Refreshtoken should be greater than the expiration of Access Token
                    .expirationTime(Instant.now().plusMillis(refreshTokenValidity))
                    .user(user)
                    .build();

//            save the newly generated RefreshToken into User Table/data
            refreshTokenRepo.save(refreshToken);
        }

//        if refreshToken is not empty then return it
        return refreshToken;
    }

//    Verifying RefreshToken Method
    public RefreshToken verifyRefreshToken(String refreshToken){
        RefreshToken refToken = refreshTokenRepo.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

//   if RefreshToken exists then comparing expirationTime with current time
//        it value is negative then time/token is expired
        if(refToken.getExpirationTime().compareTo(Instant.now()) < 0){
//      if existing RefTOken is expired then Delete its value from Table/DB
            refreshTokenRepo.delete(refToken);
            throw new RuntimeException("Refresh Token expired - Please Re-Login");
        }

//        if all good then return RefreshToken
        return refToken;
    }
}
