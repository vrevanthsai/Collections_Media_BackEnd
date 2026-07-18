package com.manga.collectionBend.service;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.repositories.UserRepo;
import com.manga.collectionBend.auth.utils.AuthResponse;
import com.manga.collectionBend.dto.ApiResponse;
import com.manga.collectionBend.dto.ChangePwdRequest;
import com.manga.collectionBend.dto.ProfileRequest;
import com.manga.collectionBend.repositories.CollectionRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;


@Service
public class ProfileService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${project.collectionImage}")
    private String path;

    public ProfileService(UserRepo userRepo, PasswordEncoder passwordEncoder, CollectionRepo collectionRepo) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    //    Get Single User data Api service method
    public ApiResponse<AuthResponse> getUser(Integer userId) {
        if(userId != null){
            UserEntity user = userRepo.findById(userId).orElse(new UserEntity());
            if(user.getUserId() != null){
                AuthResponse authResponse = AuthResponse.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .username(user.getUniqueUsername())
                        .addedDate(user.getAddedDate())
                        .imagename(user.getImageName())
                        .build();

                return ApiResponse.success(authResponse);
            } else {
                return ApiResponse.error("User not found with id: " + userId);
            }
        } else {
            return ApiResponse.error("UserId can not be null!");
        }
    }

    // Update user api service method
    public ApiResponse<AuthResponse> updateUser(Integer userId, ProfileRequest profileRequest, MultipartFile file) throws IOException {
//        check if user exists or not
        var existingUser = userRepo.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found with provided userid:" + userId));

        //        Validation check to prevent users to update his account with already existing UserNames
        UserEntity existingUserNameUser = userRepo.findByUniqueUsername(profileRequest.getUsername());
//        if userName exists, and it's userId is not equal to provided userId then we send error msg but
//        userName's userEntity's userId == to provided userId then that means it's this user's userName only - not others- so we continue with remaining steps of update
//       and if existingUserNameUser - record value is null- that means no user record has that profileRequest.username value which is new and can be updated to provided user-record/data
        if(existingUserNameUser != null){
            if(!Objects.equals(existingUserNameUser.getUserId(), userId)){
                return ApiResponse.error("Username already exists with username: " + profileRequest.getUsername() + ", try with new username!!");
            }
        }

        if(existingUser.getUserId() != null){
            // mutate the existing managed entity directly — don't rebuild it
            existingUser.setName(profileRequest.getName());
            existingUser.setUniqueUsername(profileRequest.getUsername());
            //        only store img- if file is sent from frontend - because img field is optional
            if(file != null && !file.isEmpty()) {
//            Assigning Image var values to user entity object if image/file exists only
                existingUser.setImageName(file.getOriginalFilename());
                existingUser.setImageType(file.getContentType());
                existingUser.setImageData(file.getBytes());
            }

//            save()- updates a table record/row if provided ID exists or else creates new record/row in that table
            UserEntity updatedUser = userRepo.save(existingUser);

            AuthResponse authResponse = AuthResponse.builder()
                    .userId(updatedUser.getUserId())
                    .email(updatedUser.getEmail())
                    .name(updatedUser.getName()) // edited/updated value
                    .username(updatedUser.getUniqueUsername()) // edited/updated value
                    .addedDate(updatedUser.getAddedDate())
                    .imagename(updatedUser.getImageName())
                    .build();

            return ApiResponse.success(authResponse);

        } else {
            return ApiResponse.error("User not found with id: " + userId);
        }
    }

    public ApiResponse<String> changePwdHandler(Integer userId, ChangePwdRequest changePwdRequest) {

        // check if user exists or not
        var existingUser = userRepo.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with provided userid:" + userId));

        // validate old password matches the one stored in DB
//        here decoding of old pwd is not needed because passwordEncoder has the .matches() method which automatically converts incoming request-oldPwd and compares it with stored pwd
        if (!passwordEncoder.matches(changePwdRequest.getOldPwd(), existingUser.getPassword())) {
            return ApiResponse.error("Old/Current password is incorrect");
        }

        // prevent reusing the same password
        if (passwordEncoder.matches(changePwdRequest.getNewPwd(), existingUser.getPassword())) {
            return ApiResponse.error("New password must be different from old password");
        }

        // encode and set new password
        existingUser.setPassword(passwordEncoder.encode(changePwdRequest.getNewPwd()));

        // save updated user
        userRepo.save(existingUser);

        return ApiResponse.success("Password changed successfully");
    }

    public String deleteMyAccountHandler(Integer userId) throws IOException {
        // check if user exists or not
        var existingUser = userRepo.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with provided userid:" +  userId));
        String username = existingUser.getUniqueUsername();

        // Delete/Remove All internally Stored Collection Images before deleting all User related data
//        By Deleting entire userId folder inside CollectionImages main folder where all that Deleted User's images are stored instead of deleting each image separately
        String fullFolderPath = path + File.separator + userId;  // userId-column is unique per users and does not have any special characters
        deleteUserFolder(fullFolderPath); // already-complete path passed in

//        Delete user - deletes user by matching its existing userEntity record data in Table(not by userID)
//        and all related records in other tables automatically using Hibernate BiDirectional relationship syncing
        userRepo.delete(existingUser);
        return "Your Account Deleted Successfully with Username "+ username;
    }

//    This method used for Deleting User's stored images files in his userId based folder but deleting entire folder is not possible,
//    so first we use this method to delete each stored image/files first then delete entire file to make to clean when User's data is deleted from DB
    private void deleteUserFolder(String fullPath) throws IOException {
        if (fullPath == null) {
            return; // nothing to delete
        }

        Path folderPath = Paths.get(fullPath); // just use it directly, no appending

        if (!Files.exists(folderPath)) {
            return; // folder doesn't exist, nothing to do
        }

        try (Stream<Path> walk = Files.walk(folderPath)) {
            walk.sorted(Comparator.reverseOrder()) // delete children before parent
                    .forEach(p -> {
                        try {
                            Files.delete(p); // deleting each file in folder first then entire folder
                        } catch (IOException e) {
                            throw new UncheckedIOException("Failed to delete: " + p, e);
                        }
                    });
        }
    }
}
