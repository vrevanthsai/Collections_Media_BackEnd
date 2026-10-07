package com.manga.collectionBend.dto;

import com.manga.collectionBend.entities.DefaultCategoryEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefaultCategoryDto {
    private Integer id;
    private String categoryName;
    private boolean active = true; // lets admin "retire" a default without deleting history

    //    for admin-based default categories
    public static DefaultCategoryDto fromDefaultEntity(DefaultCategoryEntity defaultCategory) {
        return DefaultCategoryDto.builder()
                .id(defaultCategory.getId())
                .categoryName(defaultCategory.getCategoryName())
                .active(defaultCategory.isActive())
                .build();
    }
}
