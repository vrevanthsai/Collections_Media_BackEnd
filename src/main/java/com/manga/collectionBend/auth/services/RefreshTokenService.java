package com.manga.collectionBend.auth.services;

import com.manga.collectionBend.auth.entities.RefreshToken;
import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.repositories.RefreshTokenRepo;
import com.manga.collectionBend.auth.repositories.UserRepo;
import jakarta.transaction.Transactional;
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
                .orElseThrow(() -> new UsernameNotFoundException("user not found with email: " + username)); // create Custom Exception in exceptions-PACKAGE TO SEND proper Error msg to client

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
// our delete happens inside a service method → it must be transactional-
// to avoid errors while deleting refreshToken-row and still that user-row is pointing to deleted refreshToken value
    @Transactional
    public RefreshToken verifyRefreshToken(String refreshToken){
        RefreshToken refToken = refreshTokenRepo.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found")); // create Custom Exception in exceptions-PACKAGE TO SEND proper Error msg to client

//   if RefreshToken exists then comparing expirationTime with current time
//        it value is negative then time/token is expired
        if(refToken.getExpirationTime().compareTo(Instant.now()) < 0){
//          clear expired refreshToken data from user table(Parent) to avoid errors
            UserEntity user = refToken.getUser();
            user.setRefreshToken(null);

//      if existing RefTOken is expired then Delete its value from Table/DB
//      when we delete child table-row then its mapping row in parent-table should also get cleared(value=null)
            refreshTokenRepo.delete(refToken);
            throw new RuntimeException("Refresh Token expired - Please Re-Login"); // create Custom Exception in exceptions-PACKAGE TO SEND proper Error msg to client
        }

//        if all good then return RefreshToken
        return refToken;
    }
}
