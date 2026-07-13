package com.manga.collectionBend.service;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.entities.UserRole;
import com.manga.collectionBend.auth.repositories.UserRepo;
import com.manga.collectionBend.auth.utils.AuthResponse;
import com.manga.collectionBend.dto.ApiResponse;
import com.manga.collectionBend.dto.ProfileRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProfileService {
    private final UserRepo userRepo;

    public ProfileService(UserRepo userRepo) {
        this.userRepo = userRepo;
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
        var existingUser = userRepo.findById(profileRequest.getUserId()).orElseThrow(() -> new UsernameNotFoundException("User not found with provided userid:" + userId));

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
//            create a userEntity object to send to userRepo
            var user = UserEntity.builder()
                    .userId(userId) // not editable
                    .email(existingUser.getEmail())  // not editable
                    .name(profileRequest.getName()) // editable
                    .username(profileRequest.getUsername()) // editable
                    .password(existingUser.getPassword()) // same previous pwd- no changes
                    .addedDate(existingUser.getAddedDate())  // not editable
                    .role(existingUser.getRole())  // not editable
                    .imageData(existingUser.getImageData()) // same previous imageData if used already added or else null
                    .imageName(existingUser.getImageName())
                    .imageType(existingUser.getImageType())
                    .build();
            //        only store img- if file is sent from frontend - because img field is optional
            if(file != null && !file.isEmpty()) {
//            Assigning Image var values to user entity object if image/file exists only
                user.setImageName(file.getOriginalFilename());
                user.setImageType(file.getContentType());
                user.setImageData(file.getBytes());
            }

//            save()- updates a table record/row if provided ID exists or else creates new record/row in that table
            UserEntity updatedUser = userRepo.save(user);

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
}
