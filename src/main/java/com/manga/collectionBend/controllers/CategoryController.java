package com.manga.collectionBend.controllers;

import com.manga.collectionBend.dto.CategoryDto;
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
}
