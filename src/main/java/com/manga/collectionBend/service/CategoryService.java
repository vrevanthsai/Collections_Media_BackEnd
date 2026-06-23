package com.manga.collectionBend.service;

import com.manga.collectionBend.dto.CategoryDto;
import com.manga.collectionBend.dto.CategoryResponse;
import com.manga.collectionBend.repositories.CategoryRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepo categoryRepo;

//    Default categories data or Admin can later edit them either manually or by using Post-Api
    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Movies",
            "Anime",
            "Series",
            "Books",
            "Games"
    );

    public CategoryService(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public List<CategoryDto> getDefaultCategories() {
        return DEFAULT_CATEGORIES.stream()
                .map(CategoryDto::new)
                .toList();
    }

    public List<CategoryResponse> getCategoriesByUser(Integer userId) {

        return categoryRepo.findByUserUserId(userId)
                .stream()
                .map(category -> {
                    CategoryResponse dto = new CategoryResponse();
                    dto.setCategoryId(category.getCategoryId());
                    dto.setCategoryName(category.getCategoryName());
                    return dto;
                })
                .toList();
    }
}