package com.manga.collectionBend.controllers;

import com.manga.collectionBend.dto.ApiResponse;
import com.manga.collectionBend.dto.CategoryDto;
import com.manga.collectionBend.dto.DefaultCategoryDto;
import com.manga.collectionBend.dto.UserDto;
import com.manga.collectionBend.entities.DefaultCategoryEntity;
import com.manga.collectionBend.repositories.DefaultCategoryRepo;
import com.manga.collectionBend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
// this is the Syntax for Pre-Authorization- Role-based - Access-Restriction(one of the Security filter) and user's without 'ADMIN' role cant access this API and throws Error(Bad Request)
//    and this is linked with UserEntity class- GrantedAuthority() method which has logged-In user role value
@PreAuthorize("hasAuthority('ADMIN')") // throw/send error msg to frontend that - you cant access Admin apis
@RequestMapping("/api/v1/user/{userId}/admin")
public class AdminController {
    private final AdminService adminService;
    private final DefaultCategoryRepo defaultCategoryRepo;

    public AdminController(AdminService adminService, DefaultCategoryRepo defaultCategoryRepo) {
        this.adminService = adminService;
        this.defaultCategoryRepo = defaultCategoryRepo;
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
    public ResponseEntity<String> deleteUser(@PathVariable Integer userAccountId) throws IOException {
        return ResponseEntity.ok(adminService.deleteUserHandler(userAccountId));
    }

//    Admin-based - Default Categories Apis
    @GetMapping("/get-all-default-categories")
    public ApiResponse<List<DefaultCategoryDto>> getAll() {
        List<DefaultCategoryDto> all = defaultCategoryRepo.findAll().stream()
            .map(DefaultCategoryDto::fromDefaultEntity)
            .toList();
        return ApiResponse.success(all);
    }

    @PostMapping("/create-default-category")
    public ApiResponse<String> addDefaultCategory(@RequestParam String name) {
        DefaultCategoryEntity entity = new DefaultCategoryEntity();
        entity.setCategoryName(name);
        entity.setActive(true);
        defaultCategoryRepo.save(entity);
        return ApiResponse.success("Default category added");
    }

    @PatchMapping("/update-default-category/{id}")
    public ApiResponse<String> editDefaultCategory(@PathVariable Integer id, @RequestParam String newName) {
        var entity = defaultCategoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Default category not found with id: " + id));

        // prevent renaming to a name that already exists on another default category
        if (defaultCategoryRepo.existsByCategoryNameAndIdNot(newName, id)) {
            return ApiResponse.error("A default category with this name already exists");
        }

        entity.setCategoryName(newName);
        defaultCategoryRepo.save(entity);
        return ApiResponse.success("Default category updated");
    }

//    instead of Deleting Default category value which will be linked to User based categories - we edit this deactivate  or activate value to false(meaning it will not be used anymore)
//    MAJORLY - This 2 Api will not be used
    @PatchMapping("/{id}/deactivate")
    public ApiResponse<String> deactivate(@PathVariable Integer id) {
        var entity = defaultCategoryRepo.findById(id).orElseThrow();
        entity.setActive(false);
        defaultCategoryRepo.save(entity);
        return ApiResponse.success("Default category deactivated");
    }
    @PatchMapping("/{id}/activate")
    public ApiResponse<String> activate(@PathVariable Integer id) {
        var entity = defaultCategoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Default category not found with id: " + id));
        entity.setActive(true);
        defaultCategoryRepo.save(entity);
        return ApiResponse.success("Default category activated");
    }
}
