package com.manga.collectionBend.controllers;

import com.manga.collectionBend.dto.CategoryDto;
import com.manga.collectionBend.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
