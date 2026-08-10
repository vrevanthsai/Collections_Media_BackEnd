package com.manga.collectionBend.service;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.repositories.UserRepo;
import com.manga.collectionBend.auth.utils.AuthResponse;
import com.manga.collectionBend.dto.*;
import com.manga.collectionBend.entities.CollectionEntity;
import com.manga.collectionBend.repositories.CollectionRepo;
import com.manga.collectionBend.repositories.FriendConnectionRepo;
import com.manga.collectionBend.utils.CollectionProgress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


@Service
public class ProfileService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final CollectionRepo collectionRepo;
    private final FriendConnectionRepo friendConnectionRepo;

    @Value("${project.collectionImage}")
    private String path;

    public ProfileService(UserRepo userRepo, PasswordEncoder passwordEncoder, CollectionRepo collectionRepo, CollectionRepo collectionRepo1, FriendConnectionRepo friendConnectionRepo) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.collectionRepo = collectionRepo1;
        this.friendConnectionRepo = friendConnectionRepo;
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

    // here searchValue- can be user's username or collection's name
    public SearchResultDto searchUserOrCollectionHandler(String searchValue, UserEntity currentUser) {

        if (searchValue == null || searchValue.isBlank()) {
            return SearchResultDto.builder()
                    .users(List.of())
                    .collections(List.of())
                    .build();
        }

        // search by username (exclude searching for yourself)
        UserEntity user = userRepo.findByUniqueUsername(searchValue);
        List<FriendDto> userResults = new ArrayList<>();
        if (user != null && !user.getUserId().equals(currentUser.getUserId())) {
            userResults.add(FriendDto.fromEntity(user));
        }

        // search within current user's own collections by name (case-insensitive partial match)
        List<CollectionEntity> userBasedCollections = collectionRepo.findByUserId(currentUser);
        List<CollectionSearchDto> collectionResults = userBasedCollections.stream()
                .filter(collection -> collection.getName().toLowerCase().contains(searchValue.toLowerCase()))
                .map(CollectionSearchDto::fromEntity)
                .toList();

//        This SearchResultDto is an object{} which has 2 properties where 1 is list for users(searched by username) and 2nd for list for collections(searched by collection's name)
//        so that frontend - we can get/access both type of search results based on provided search value from navbar-search input value
        return SearchResultDto.builder()
                .users(userResults)
                .collections(collectionResults)
                .build();
    }

    public UserViewDto userViewPageHandler(Integer currentUserId, Integer otherUserId) {
        // return empty data if userIds are not provided
        if (otherUserId == null || currentUserId == null) {
            return UserViewDto.builder()
                    .user(new FriendDto())
                    .collections(List.of())
                    .build();
        }
//        search/find otherUserId - data/record in User table
        UserEntity otherUser = userRepo.findById(otherUserId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with provided userid:" + otherUserId));
        FriendDto otherUserDto = new FriendDto();
        if (!otherUser.getUserId().equals(currentUserId)) { // prevent adding user data if other IDs are same to prevent currentUser to view his own user view page
            otherUserDto = FriendDto.fromEntity(otherUser);
        } else {
            throw new IllegalStateException("Both IDs should not be the same");
        }

//        search/find his collections data/list which are marked as PUBLIC as privacy
        List<CollectionEntity> otherUserBasedCollections = collectionRepo.findByUserId(otherUser); //search by userEntity
        List<CollectionDto> filteredOtherUserCollections = new ArrayList<>();

        //        iterate through the list and generate imageURL for each collection obj of retrieved data objects from DB and
//        map to CollectionDto object
        for(CollectionEntity collection : otherUserBasedCollections) {
            // here we dont need to send collectionImage URL to frontend for User View page - where we only show cards without images needed
//            we only need image loading in Single Collection page - so sending with image details are not needed here
            String collectionUrl = "";
//            First Store all OtherUser's collections list marked as PUBLIC - even if he is a stranger or a frind of currentUser
            if(collection.getPrivacy().equals(CollectionProgress.PUBLIC)) {
                CollectionDto collectionDto = new CollectionDto(
                        collection.getCollectionId(),
                        collection.getName(),
                        collection.getCategory().getCategoryId(),
                        collection.getUserId().getUserId(),
                        collection.getUserId().getUniqueUsername(),
                        collection.getRating(),
                        collection.getReview(),
                        collection.getProgress(),
                        collection.getPrivacy(),
                        collection.getAddedDate(),
                        collection.getImagename(),
                        collectionUrl
                );
                collectionDto.setCategoryName(collection.getCategory().getCategoryName());
                filteredOtherUserCollections.add(collectionDto);
            }

//            Next - Check if currentUser and OtherUser is friends or not
            boolean isFriend = friendConnectionRepo.existsBetween(currentUserId, otherUserId);
            if(isFriend) {
                // if both are friends then add/send otherUser collections which are Marked as FRIENDS as a privacy
                if(collection.getPrivacy().equals(CollectionProgress.FRIENDS)) {
                    CollectionDto collectionDto = new CollectionDto(
                            collection.getCollectionId(),
                            collection.getName(),
                            collection.getCategory().getCategoryId(),
                            collection.getUserId().getUserId(),
                            collection.getUserId().getUniqueUsername(),
                            collection.getRating(),
                            collection.getReview(),
                            collection.getProgress(),
                            collection.getPrivacy(),
                            collection.getAddedDate(),
                            collection.getImagename(),
                            collectionUrl
                    );
                    collectionDto.setCategoryName(collection.getCategory().getCategoryName());
                    filteredOtherUserCollections.add(collectionDto);
                }
            }
        }

 //        This UserViewDto is an object{} which has 2 properties where 1 is list for user-min-details and 2nd for list for collections(which are marked as Public or Friends)
//        so that frontend - we can get/access both type of otherUser min details and his Collections list in one DTO object
        return UserViewDto.builder()
                .user(otherUserDto)
                .collections(filteredOtherUserCollections)
                .build();
    }
}
