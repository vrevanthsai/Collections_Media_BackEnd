package com.manga.collectionBend.auth.repositories;

import com.manga.collectionBend.auth.entities.ForgotPassword;
import com.manga.collectionBend.auth.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForgotPasswordRepo extends JpaRepository<ForgotPassword, Integer> {

//    custom method to fetch data depending on otp and user values
//    this is the custom JPQL query and replaces 1,2 positions with provided method-params
    @Query("select fp from ForgotPassword fp where fp.otp = ?1 and fp.user = ?2")
    Optional<ForgotPassword> findByOtpAndUser(Integer otp, UserEntity user);

}
