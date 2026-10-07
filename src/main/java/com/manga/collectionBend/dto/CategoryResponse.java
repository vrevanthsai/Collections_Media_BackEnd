package com.manga.collectionBend.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CategoryResponse {
    private Integer categoryId;
    private String categoryName;
    private boolean isEditable;
    private Integer defaultCategoryId; // null for custom categories, set for default-sourced ones
}
