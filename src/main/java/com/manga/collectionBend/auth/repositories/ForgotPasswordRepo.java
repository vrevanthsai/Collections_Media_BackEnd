package com.manga.collectionBend.auth.repositories;

import com.manga.collectionBend.auth.entities.ForgotPassword;
import com.manga.collectionBend.auth.entities.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForgotPasswordRepo extends JpaRepository<ForgotPassword, Integer> {

//    custom method to fetch data depending on otp and user values
//    this is the custom JPQL query and replaces 1,2 positions with provided method-params
    @Query("select fp from ForgotPassword fp where fp.otp = ?1 and fp.user = ?2")
    Optional<ForgotPassword> findByOtpAndUser(Integer otp, UserEntity user);

//    custom method to fetch fp-row data from FP table based on email-field(of user)
//    used naming formate- findBy+CapitalFirstLetter of Field/Column name instead of custom JPQL
    Optional<ForgotPassword> findByEmail(String email);

//    custom method to update verification_status column when OTP verified successfully in 2nd Api logic
//    we use these 2 annotations when ever we try to do modify the data in DB(like update,delete)
    @Transactional
    @Modifying
//    custom method to update verificationStatus field only
    @Query("update ForgotPassword fp set fp.verificationStatus = ?2 where fp.email = ?1")
    void updateVerificationStatus(String email, Boolean verificationStatus);
}
