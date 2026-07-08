package com.manga.collectionBend.controllers;

import com.manga.collectionBend.auth.utils.AuthResponse;
import com.manga.collectionBend.dto.ApiResponse;
import com.manga.collectionBend.dto.ProfileRequest;
import com.manga.collectionBend.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/user/{userId}/profile")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    //    User Profile APIs
    //    Get Single User details API
    @GetMapping("/getuser")
    public ResponseEntity<ApiResponse<AuthResponse>> getUserHandler(@PathVariable Integer userId){ // userId param value comes from parent/top RequestMapping({userId})
        return ResponseEntity.ok(profileService.getUser(userId));
    }

//    Update User API
    @PutMapping("/update-user")
    public ResponseEntity<ApiResponse<AuthResponse>> updateUserHandler(@PathVariable Integer userId, @RequestBody ProfileRequest profileRequest){ // userId param value comes from parent/top RequestMapping({userId})
        return ResponseEntity.ok(profileService.updateUser(userId, profileRequest));
    }

//    Delete Single User API
}
