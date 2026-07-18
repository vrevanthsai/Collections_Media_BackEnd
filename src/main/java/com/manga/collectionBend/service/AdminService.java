package com.manga.collectionBend.service;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.repositories.UserRepo;
import com.manga.collectionBend.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
public class AdminService {
    private final UserRepo userRepo;

    @Value("${project.collectionImage}")
    private String path;

    public AdminService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public List<UserDto> getAllUsersHandler() {
        List<UserEntity> users = userRepo.findAll();
        List<UserDto> userDtos = new ArrayList<>();

        for (UserEntity user : users) {
            UserDto userDto = new UserDto(
                    user.getUserId(),
                    user.getName(),
                    user.getUniqueUsername(),
                    user.getEmail(),
                    user.getRole(),
                    user.isSuspended(),
                    user.getAddedDate()
            );
            userDtos.add(userDto);
        }

        return userDtos;
    }

//    Search User is done by either user's username or email
    public List<UserDto> searchUserHandler(String searchValue) {
        UserEntity user = userRepo.findByUsernameOrEmail(searchValue);
        List<UserDto> userDtos = new ArrayList<>();

        if (user != null) {
            UserDto userDto = new UserDto(
                    user.getUserId(),
                    user.getName(),
                    user.getUniqueUsername(),
                    user.getEmail(),
                    user.getRole(),
                    user.isSuspended(),
                    user.getAddedDate()
            );
            userDtos.add(userDto);
            return userDtos;
        } else  {
            return userDtos; // if searchValue does not have any matching user-records in DB then return [] empty array
        }
    }

    //    Here- userAccountId- is required User's userId(we already have userId being using above in parentMapping- so we use new)
//    and suspendValue- is String Value- where if value is equal to "suspend" then in DB it will be marked as True
//    and if value is "activate" then in DB it will be marked as False(default value)
    public String suspendUserHandler(Integer userAccountId, String suspendValue) {
        // check if user exists or not
        var existingUser = userRepo.findById(userAccountId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with provided userid:" + userAccountId));

        if(Objects.equals(suspendValue, "suspend")) {
            existingUser.setSuspended(true);
            // save updated user- here we directly save already existing user data with only suspended column value is updated and remaining all are same
//            instead of creating new UserEntity object and re-entering each remaining values everytime
            userRepo.save(existingUser);
            return "This user has been Suspended with UserId: " + existingUser.getUserId();
        } else if(Objects.equals(suspendValue, "activate")) {
            existingUser.setSuspended(false);
            // save updated user
            userRepo.save(existingUser);
            return "This user has been Activated with UserId: " + existingUser.getUserId();
        } else {
            return "Incorrect suspendValue give- only send with suspend or activate";
        }

    }

//    This Admin based- User Delete undergoes OTP verification process
    public String deleteUserHandler(Integer userAccountId) throws IOException {
        // check if user exists or not
        var existingUser = userRepo.findById(userAccountId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with provided userid:" + userAccountId));
        String username = existingUser.getUniqueUsername();

        // Delete/Remove All internally Stored Collection Images before deleting all User related data
//        By Deleting entire userId folder inside CollectionImages main folder where all that Deleted User's images are stored instead of deleting each image separately
        String fullFolderPath = path + File.separator + userAccountId;  // userId-column is unique per users and does not have any special characters
        deleteUserFolder(fullFolderPath); // already-complete path passed in

//        Delete user - deletes user by matching its existing userEntity record data in Table(not by userID)
        userRepo.delete(existingUser);
        return "User Deleted Successfully with Username "+ username;
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
