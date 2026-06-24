package com.manga.collectionBend.service;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.repositories.UserRepo;
import com.manga.collectionBend.dto.CategoryDto;
import com.manga.collectionBend.dto.CategoryRequest;
import com.manga.collectionBend.dto.CategoryResponse;
import com.manga.collectionBend.entities.CategoryEntity;
import com.manga.collectionBend.exceptions.CollectionNotFoundExpception;
import com.manga.collectionBend.repositories.CategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepo categoryRepo;
    @Autowired
    private UserRepo userRepo;

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

    public CategoryResponse addCategoryHandler(CategoryRequest categoryRequest) {
//        get userId from requestBody from frontend
        Integer userId = categoryRequest.getUserId();
//        based on userId value- get UserEntity reference data to link it/store it in Category table-column
        UserEntity user = userRepo.findById(userId).orElse(null);
        if (user != null) {
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setCategoryName(categoryRequest.getCategoryName());
            categoryEntity.setUser(user);
//            save the data
            CategoryEntity savedCategory = categoryRepo.save(categoryEntity);
//            return sample data after saving
            CategoryResponse dto = new CategoryResponse();
            dto.setCategoryId(savedCategory.getCategoryId());
            dto.setCategoryName(savedCategory.getCategoryName());
            return dto;
        } else {
//            return empty object if user data not found with given userId from frontend
            return new CategoryResponse();
        }
    }

    public CategoryResponse updateCategoryHandler(Integer categoryId, CategoryRequest categoryRequest) {
//       Validation check - get Category data based on provided category-id
        CategoryEntity existingCategory = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id = " + categoryId));

        CategoryEntity categoryEntity = new CategoryEntity();
//            setting values to new entity object which has updated categoryName value and remaining 2 field - id and User values will be same as previous/existing category record-data
        categoryEntity.setCategoryId(existingCategory.getCategoryId()); // providing id which will update this ID's record in table
        categoryEntity.setCategoryName(categoryRequest.getCategoryName()); // new updated value
        categoryEntity.setUser(existingCategory.getUser()); // old value

//        save the updated data into category table
        CategoryEntity updatedCategory = categoryRepo.save(categoryEntity);

        CategoryResponse dto = new CategoryResponse();
        dto.setCategoryId(updatedCategory.getCategoryId());
        dto.setCategoryName(updatedCategory.getCategoryName());
        return dto;
    }
}