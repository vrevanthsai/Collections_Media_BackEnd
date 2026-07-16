package com.manga.collectionBend.controllers;

import com.manga.collectionBend.dto.UserDto;
import com.manga.collectionBend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
// this is the Syntax for Pre-Authorization- Role-based - Access-Restriction(one of the Security filter) and user's without 'ADMIN' role cant access this API and throws Error(Bad Request)
//    and this is linked with UserEntity class- GrantedAuthority() method which has logged-In user role value
@PreAuthorize("hasAuthority('ADMIN')") // throw/send error msg to frontend that - you cant access Admin apis
@RequestMapping("/api/v1/user/{userId}/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    //    GET- All Users API
    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UserDto>> getAllUsers(){
        return ResponseEntity.ok(adminService.getAllUsersHandler());
    }

//    Get- Search User Api by either Username or Email
    @GetMapping("/search-user/{searchValue}")
    public ResponseEntity<List<UserDto>> searchUser(@PathVariable String searchValue){
        return ResponseEntity.ok(adminService.searchUserHandler(searchValue));
    }

//    Put- Suspend/Activate User account Api
//    Here- userAccountId- is required User's userId(we already have userId being using above in parentMapping- so we use new)
//    and suspendValue- is String Value- where if value is equal to "suspend" then in DB it will be marked as True
//    and if value is "activate" then in DB it will be marked as False(default value)
    @PutMapping("/suspend-user/{userAccountId}/{suspendValue}")
    public ResponseEntity<String> suspendUser(@PathVariable Integer userAccountId, @PathVariable String suspendValue){
        return ResponseEntity.ok(adminService.suspendUserHandler(userAccountId, suspendValue));
    }

//    Admin based- Delete User Api
    @DeleteMapping("/delete-user/{userAccountId}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer userAccountId){
        return ResponseEntity.ok(adminService.deleteUserHandler(userAccountId));
    }
}
