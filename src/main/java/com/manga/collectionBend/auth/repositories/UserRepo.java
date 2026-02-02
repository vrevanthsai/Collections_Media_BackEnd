package com.manga.collectionBend.auth.repositories;

import com.manga.collectionBend.auth.entities.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<UserEntity, Integer> {

//    JpaRepository class does not have specific field finding methods-so we create them
//    findByUsername or findByEmail- this is the convention of JPA to write custom Repo method to automatically generate its own query to find/fetch our specific field from DB
//    naming formate- findBy+CapitalFirstLetter of Field/Column name(eg-Username) or you can create a sql query to fetch data
//    we are using Email field for authentication for Security-Username value - so we use findbyEmail
    Optional<UserEntity> findByEmail(String username);

    //    we use these 2 annotations when ever we try to do modify the data in DB(like update,delete)
    @Transactional
    @Modifying
    //    custom method to update password field only
    @Query("update UserEntity u set u.password = ?2 where u.email = ?1")
    void updatePassword(String email, String newPassword);
}
