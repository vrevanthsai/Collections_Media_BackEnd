package com.manga.collectionBend.service;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.repositories.UserRepo;
import com.manga.collectionBend.dto.UserDto;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AdminService {
    private final UserRepo userRepo;

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
    public String deleteUserHandler(Integer userAccountId) {
        // check if user exists or not
        var existingUser = userRepo.findById(userAccountId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with provided userid:" + userAccountId));
        String username = existingUser.getUniqueUsername();

//        Delete user - deletes user by matching its existing userEntity record data in Table(not by userID)
        userRepo.delete(existingUser);
        return "User Deleted Successfully with Username "+ username;
    }
}
