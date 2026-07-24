package com.manga.collectionBend.service;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.repositories.UserRepo;
import com.manga.collectionBend.dto.*;
import com.manga.collectionBend.entities.CategoryEntity;
import com.manga.collectionBend.entities.CollectionEntity;
import com.manga.collectionBend.repositories.CategoryRepo;
import com.manga.collectionBend.repositories.CollectionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Service
public class CategoryService {

    private final CategoryRepo categoryRepo;
    @Autowired
    private UserRepo userRepo;
    private final CollectionRepo collectionRepo;

    @Value("${project.collectionImage}")
    private String path;

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

    public ApiResponse<CategoryResponse> addCategoryHandler(CategoryRequest categoryRequest, Integer userId) {
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

    public CategoryResponse updateCategoryHandler(Integer categoryId, CategoryRequest categoryRequest, Integer userId) {
//       Validation check - get Category data based on provided category-id
        CategoryEntity existingCategory = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id = " + categoryId));

//        UserId Validation check- to see if same user is trying to update his data or some one
//        if same - we update or not same - we throw error
        if(Objects.equals(userId, existingCategory.getUser().getUserId())) {
            // mutate the existing managed entity directly — don't rebuild it
//            setting values to already existing entity object which has updated categoryName value and remaining 2 field - id and User values will be same as previous/existing category record-data
            existingCategory.setCategoryName(categoryRequest.getCategoryName()); // new updated value

//        save the updated data into category table
//      always use existing categoryObject for updating any record data in DB- dont use separate/new CategoryEntity object/var
//      then no relational conflicts issue(auto-delete of collection record due to adding new object instead of using existing object)
//      will not come between this parent(category) entity and its child(collection) entity
            CategoryEntity updatedCategory = categoryRepo.save(existingCategory);

            CategoryResponse dto = new CategoryResponse();
            dto.setCategoryId(updatedCategory.getCategoryId());
            dto.setCategoryName(updatedCategory.getCategoryName());
            return dto;
        } else {
            throw new IllegalStateException("You userId: "+ userId +" are not authorized to update other user's data!");
        }
    }

    public CategoryDeleteResponse deleteCategoryHandler(Integer categoryId, Integer userId) {
        //       Validation check - get Category data based on provided category-id
        CategoryEntity existingCategory = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id = " + categoryId));

        String categoryName = existingCategory.getCategoryName();
        //        UserId Validation check- to see if same user is trying to update his data or some one
//        if same - we delete or not same - we throw error
        if(Objects.equals(userId, existingCategory.getUser().getUserId())) {
//        Logic to prevent Category Deletion if that category has some created collections which are linked/mapped to this wanted category
//        here when Category-record/row will act like parent record which is referred by foreign-key in a Collection-record/row(child) -
//        so delection of parent record(category) will not work until its referred child record in Collection table is deleted first then only deletion of category-record will work next
            List<CollectionEntity> categoryBasedCollections = collectionRepo.findByCategory(existingCategory);

//        delete category based collections images logic to delete each collection image separately-
//        - because we cant delete entire userId-main folder of User just to delete few collectionImages which are only linked to this CategoryId
            if (categoryBasedCollections != null && !categoryBasedCollections.isEmpty()) {
                categoryBasedCollections.forEach(collection -> {
                    //        only delete file/imagge in CollectionImages-backend-folder if in Db it has imageName stored(means user given img while creating this collection) or else imageName="" empty(means user did no give any img which does not need deleting anything)
                    if(collection.getImagename() != null && !Objects.equals(collection.getImagename(), "")){
                        String newPath = path + File.separator + userId;
                        //        delete the file/image associated with this object which will be deleted from table/db
                        try {
                            Files.deleteIfExists(Paths.get(newPath + File.separator + collection.getImagename())); // deletes empty folder or single file once
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

//  Here category record data will get deleted and automatically its linked collection records will also get deleted by Hibernate-Cascade.ALL
            categoryRepo.delete(existingCategory);
            CategoryDeleteResponse categoryDeleteResponse = new CategoryDeleteResponse();
            categoryDeleteResponse.setSuccess(Boolean.TRUE);
            categoryDeleteResponse.setMessage("Category deleted successfully with name = " + categoryName);
            return categoryDeleteResponse;
        }else {
            throw new IllegalStateException("You userId: "+ userId +" are not authorized to delete other user's data!");
        }
    }
}