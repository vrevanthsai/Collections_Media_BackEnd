package com.manga.collectionBend.controllers;

import com.manga.collectionBend.dto.*;
import com.manga.collectionBend.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/user/{userId}/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

//    Get Categories data based on used id and send array-objects to frontend with category_id and category_name
    @GetMapping("/get-user-categories")
//    userId param value is linked in above parent RequestMapping() and it will be used in PathVariable- where both vars name are same
    public ResponseEntity<List<CategoryResponse>> getCategoriesByUser(
            @PathVariable Integer userId) {

        return ResponseEntity.ok(
                categoryService.getCategoriesByUser(userId));
    }

//    POST-Api - Add category
    @PostMapping("/add-category")
//    if only Json/one part is being sent from frontend then we can use @RequestBody and if we have more then 2 parts - we use @RequestPart for receiving data from frontend Api call
    public ResponseEntity<ApiResponse<CategoryResponse>> addCategoryHandler(@RequestBody CategoryRequest categoryRequest, @PathVariable Integer userId) { // userId from parent-Mapping path- to prevent users to access data of other user
        ApiResponse<CategoryResponse> response = categoryService.addCategoryHandler(categoryRequest, userId);
//        send success=false and error msg with Conflict status code- 409 - when any error res comes from service-method
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
//        send success=true, with CategoryResponse data object when no errors are there
        return ResponseEntity.ok(response);
    }

//    Update-Api category
    @PutMapping("/update-category/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategoryHandler(@PathVariable Integer categoryId, @RequestBody CategoryRequest categoryRequest, @PathVariable Integer userId) {
        return ResponseEntity.ok(
                categoryService.updateCategoryHandler(categoryId, categoryRequest, userId)
        );
    }

//    Delete-Api category
    @DeleteMapping("/delete-category/{categoryId}")
    public ResponseEntity<CategoryDeleteResponse> deleteCategoryHandler(@PathVariable Integer categoryId, @PathVariable Integer userId) {
        return ResponseEntity.ok(
                categoryService.deleteCategoryHandler(categoryId, userId)
        );
    }
}
