package com.manga.collectionBend.service;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.repositories.UserRepo;
import com.manga.collectionBend.dto.*;
import com.manga.collectionBend.entities.CategoryEntity;
import com.manga.collectionBend.entities.CollectionEntity;
import com.manga.collectionBend.repositories.CategoryRepo;
import com.manga.collectionBend.repositories.CollectionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CategoryService {

    private final CategoryRepo categoryRepo;
    @Autowired
    private UserRepo userRepo;
    private final CollectionRepo collectionRepo;

//    Default categories data or Admin can later edit them either manually or by using Post-Api
    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Movies",
            "Anime",
            "Series",
            "Books",
            "Games"
    );

    public CategoryService(CategoryRepo categoryRepo, CollectionRepo collectionRepo) {
        this.categoryRepo = categoryRepo;
        this.collectionRepo = collectionRepo;
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

    public ApiResponse<CategoryResponse> addCategoryHandler(CategoryRequest categoryRequest) {
//        get userId from requestBody from frontend
        Integer userId = categoryRequest.getUserId();
//        based on userId value- get UserEntity reference data to link it/store it in Category table-column
        UserEntity user = userRepo.findById(userId).orElse(null);

        if (user == null) {
            return ApiResponse.error("User not found with ID: " + userId);
        }

        List<CategoryEntity> categories = categoryRepo.findByUserUserId(userId);

        // Check for duplicate category name (case-insensitive) to prevent duplicate data creations
        boolean isDuplicate = categories.stream()
                .anyMatch(category -> category.getCategoryName()
                        .equalsIgnoreCase(categoryRequest.getCategoryName().trim()));

        if (isDuplicate) {
            return ApiResponse.error("Category '" + categoryRequest.getCategoryName() + "' already exists.");
        }

        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setCategoryName(categoryRequest.getCategoryName());
        categoryEntity.setUser(user);
//      save the data
        CategoryEntity savedCategory = categoryRepo.save(categoryEntity);
//      return sample data after saving
        CategoryResponse dto = new CategoryResponse();
        dto.setCategoryId(savedCategory.getCategoryId());
        dto.setCategoryName(savedCategory.getCategoryName());
        return ApiResponse.success(dto);
    }

    public CategoryResponse updateCategoryHandler(Integer categoryId, CategoryRequest categoryRequest) {
//       Validation check - get Category data based on provided category-id
        CategoryEntity existingCategory = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id = " + categoryId));

//        UserId Validation check- to see if same user is trying to update his data or some one
//        if same - we update or not same - we throw error
        if(Objects.equals(categoryRequest.getUserId(), existingCategory.getUser().getUserId())) {
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
        } else {
            System.out.println("Error - provided UserId is not matching with existing category Id, hence not updating any data!!!");
            return new CategoryResponse();
        }
    }

    public CategoryDeleteResponse deleteCategoryHandler(Integer categoryId) {
        //       Validation check - get Category data based on provided category-id
        CategoryEntity existingCategory = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id = " + categoryId));

        String categoryName = existingCategory.getCategoryName();
//        Logic to prevent Category Deletion if that category has some created collections which are linked/mapped to this wanted category
//        here when Category-record/row will act like parent record which is referred by foreign-key in a Collection-record/row(child) -
//        so delection of parent record(category) will not work until its referred child record in Collection table is deleted first then only deletion of category-record will work next
        List<CollectionEntity> categoryBasedCollections = collectionRepo.findByCategory(existingCategory);

        if(categoryBasedCollections.isEmpty()){
            categoryRepo.delete(existingCategory);
            CategoryDeleteResponse categoryDeleteResponse = new CategoryDeleteResponse();
            categoryDeleteResponse.setSuccess(Boolean.TRUE);
            categoryDeleteResponse.setMessage("Category deleted successfully with name = " + categoryName);
            return categoryDeleteResponse;
        } else {
            CategoryDeleteResponse categoryDeleteResponse = new CategoryDeleteResponse();
            categoryDeleteResponse.setSuccess(Boolean.FALSE);
            categoryDeleteResponse.setMessage(
                    "This " + categoryName + " Category has "+ categoryBasedCollections.toArray().length
                            +" collections created with it, so until the collections linked to this Category are deleted, we can't delete it, please delete them all first and come back to delete this category next!!!"
                            +"Tip- 1) you can find the linked collections of a Category in Home page where you use the filters options of category and find all the linked collections of a Category in Home page or"
                            +" 2) you can use Edit option to change the category name instead of deleting it."
            );
            return categoryDeleteResponse;
        }
    }
}