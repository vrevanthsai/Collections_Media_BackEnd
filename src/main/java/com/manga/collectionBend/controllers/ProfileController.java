package com.manga.collectionBend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.repositories.UserRepo;
import com.manga.collectionBend.auth.utils.AuthResponse;
import com.manga.collectionBend.dto.*;
import com.manga.collectionBend.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/user/{userId}/profile")
public class ProfileController {
    private final ProfileService profileService;
    private final UserRepo userRepo;

    public ProfileController(ProfileService profileService, UserRepo userRepo) {
        this.profileService = profileService;
        this.userRepo = userRepo;
    }

    //    User Profile APIs
    //    Get Single User details API
    @GetMapping("/getuser")
    public ResponseEntity<ApiResponse<AuthResponse>> getUserHandler(@PathVariable Integer userId){ // userId param value comes from parent/top RequestMapping({userId})
        return ResponseEntity.ok(profileService.getUser(userId));
    }

//    Update User API
    @PutMapping("/update-user")
    public ResponseEntity<ApiResponse<AuthResponse>> updateUserHandler(@PathVariable Integer userId, // userId param value comes from parent/top RequestMapping({userId})
                                                                       @RequestPart String profileRequest,
                                                                       @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
//        Converting profileRequest input from sttriing/text to Json/class object- when sending/calling this api from Postman-form based Body data
        ProfileRequest profileRequestDto = convertToProfileRequestDto(profileRequest);

        return ResponseEntity.ok(profileService.updateUser(userId, profileRequestDto, file));
    }

    private ProfileRequest convertToProfileRequestDto(String profileRequestObj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.readValue() is used for mapping/converting given string/other(1st args) data to required class object(2nd args) and returns that converted class object
        ProfileRequest profileRequestDto = objectMapper.readValue(profileRequestObj, ProfileRequest.class);
        return profileRequestDto;
    };

//    Get Profile pic/Avatar image Api- it sends direct image to Client using Http-request body
    @GetMapping("/get-user-image")
    public ResponseEntity<byte[]> getImageByUserId(@PathVariable Integer userId){ // userId param value comes from parent/top RequestMapping({userId})
        UserEntity user = userRepo.findById(userId).orElse(null);

        if(user != null){
            byte[] imageFile = user.getImageData();
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(user.getImageType()))
                    .body(imageFile);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND) ;
        }
    }

//    Change User Password Api
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@PathVariable Integer userId, @RequestBody ChangePwdRequest changePwdRequest){
        ApiResponse<String> response = profileService.changePwdHandler(userId, changePwdRequest);
        //   send success=false and error msg with Conflict status code- 409 - when any error res comes from service-method
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
//        send success=true, with AuthResponse data object when no errors are there
        return ResponseEntity.ok(response);
    }

//    Delete Single User API
    @DeleteMapping("/delete-my-account")
    public ResponseEntity<String> deleteMyAccount(@PathVariable Integer userId) throws IOException { // userId from parent Mapping path
        return ResponseEntity.ok(profileService.deleteMyAccountHandler(userId));
    }

//  search user(username) + collection(name) api
//    In frontend or postman - PathVariable- we give /search/rinku (/search/{query} - {query} name must be same as below method parameter var name which uses PathVariable)
//    RequestParam- using path- /search?query=${rinku} (?key=value format) and ?query name must be same as below method parameter var name which users RequestParam
    @GetMapping("/search-user-or-collection") // here searchValue- can be user's username or collection's name
    public ApiResponse<SearchResultDto> searchUserOrCollection(@RequestParam String query, @AuthenticationPrincipal UserEntity currentUser) {
        return ApiResponse.success(profileService.searchUserOrCollectionHandler(query, currentUser));
    }

//    create user-view api which sends basic user details for top section of User-View page
//    and sends Collections data/list of provided userId(user) which are marked as Public for bottom section of User-View page
//    and if that user(B) is friend of requested user(A) request to view B profile/User-View page then send Collection data which has both marked as Public and Friends
}
