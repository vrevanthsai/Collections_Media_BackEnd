package com.manga.collectionBend.service;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.repositories.UserRepo;
import com.manga.collectionBend.dto.UserDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
                    user.getAddedDate()
            );
            userDtos.add(userDto);
            return userDtos;
        } else  {
            return userDtos; // if searchValue does not have any matching user-records in DB then return [] empty array
        }
    }
}
