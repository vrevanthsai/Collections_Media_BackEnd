package com.manga.collectionBend.controllers;

import com.manga.collectionBend.dto.CategoryDeleteResponse;
import com.manga.collectionBend.dto.CategoryDto;
import com.manga.collectionBend.dto.CategoryRequest;
import com.manga.collectionBend.dto.CategoryResponse;
import com.manga.collectionBend.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

//    this Api endpoint does not require security- so excluded it in SecurityConfig
    @GetMapping("/default")
    public ResponseEntity<List<CategoryDto>> getDefaultCategories() {
        return ResponseEntity.ok(
                categoryService.getDefaultCategories()
        );
    }

//    Get Categories data based on used id and send array-objects to frontend with category_id and category_name
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByUser(
            @PathVariable Integer userId) {

        return ResponseEntity.ok(
                categoryService.getCategoriesByUser(userId));
    }

//    POST-Api - Add category
    @PostMapping("/add-category")
//    if only Json/one part is being sent from frontend then we can use @RequestBody and if we have more then 2 parts - we use @RequestPart for receiving data from frontend Api call
    public ResponseEntity<CategoryResponse> addCategoryHandler(@RequestBody CategoryRequest categoryRequest) {
        return ResponseEntity.ok(
                categoryService.addCategoryHandler(categoryRequest)
        );
    }

//    Update-Api category
    @PutMapping("/update-category/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategoryHandler(@PathVariable Integer categoryId, @RequestBody CategoryRequest categoryRequest) {
        return ResponseEntity.ok(
                categoryService.updateCategoryHandler(categoryId, categoryRequest)
        );
    }

//    Delete-Api category
    @DeleteMapping("/delete-category/{categoryId}")
    public ResponseEntity<CategoryDeleteResponse> deleteCategoryHandler(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(
                categoryService.deleteCategoryHandler(categoryId)
        );
    }
}
