package com.manga.collectionBend.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CategoryRequest {
//    same naming must be in frontend object-keys when sending data to /add-category Post-Api
    private Integer userId;
    private String categoryName;
}
