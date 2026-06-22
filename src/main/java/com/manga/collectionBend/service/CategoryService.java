package com.manga.collectionBend.service;

import com.manga.collectionBend.dto.CategoryDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

//    Default categories data or Admin can later edit them either manually or by using Post-Api
    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Movies",
            "Anime",
            "Series",
            "Books",
            "Games"
    );

    public List<CategoryDto> getDefaultCategories() {
        return DEFAULT_CATEGORIES.stream()
                .map(CategoryDto::new)
                .toList();
    }
}