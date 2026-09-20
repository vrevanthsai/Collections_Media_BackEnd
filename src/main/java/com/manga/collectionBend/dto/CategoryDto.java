package com.manga.collectionBend.dto;

import com.manga.collectionBend.entities.CategoryEntity;
import com.manga.collectionBend.entities.DefaultCategoryEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {

    private Integer id;
    private String categoryName; // this now always holds the CORRECT, live name regardless of source
    private boolean isEditable;

    // for regular/custom user-owned categories
    public static CategoryDto fromEntity(CategoryEntity category) {
        return CategoryDto.builder()
                .id(category.getCategoryId())
                .categoryName(category.getEffectiveCategoryName()) // always resolves correctly
                .isEditable(category.isEditable())
                .build();
    }

//    for admin-based default categories
    public static CategoryDto fromDefaultEntity(DefaultCategoryEntity defaultCategory) {
        return CategoryDto.builder()
                .id(defaultCategory.getId())
                .categoryName(defaultCategory.getCategoryName())
                .isEditable(false)
                .build();
    }
}
