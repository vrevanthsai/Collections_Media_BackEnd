package com.manga.collectionBend.dto;

import com.manga.collectionBend.entities.CollectionEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicCollectionDto {
    private Integer collectionId;
    private String name;
    private Integer categoryId;
    private String categoryName;
    private Integer defaultCategoryId;
    private Integer userId;
    private String username;
    private Integer rating;
    private String progress;
    private String addedDate;

//    this methods maps all entity record data to this Dto based vars into a single object
    public static PublicCollectionDto fromEntity(CollectionEntity collectionEntity) {
        return PublicCollectionDto.builder()
                .collectionId(collectionEntity.getCollectionId())
                .name(collectionEntity.getName())
                .categoryId(collectionEntity.getCategory().getCategoryId())
                // getEffectiveCategoryName()- only this method will return correct categoryName- either it may be custom or default
                .categoryName(collectionEntity.getCategory().getEffectiveCategoryName())
                // if this var returns int value(id- will be user in frontend community page category based filters) then this collection's category was created by default-category by user or if it is null then it's a custom category created by user himself(then refer categoryId directly)
                .defaultCategoryId(collectionEntity.getCategory().getDefaultCategory() != null ? collectionEntity.getCategory().getDefaultCategory().getId() : null)
                .userId(collectionEntity.getUserId().getUserId())
                .username(collectionEntity.getUserId().getUniqueUsername())
                .rating(collectionEntity.getRating())
                .progress(collectionEntity.getProgress())
                .addedDate(collectionEntity.getAddedDate())
                .build();
    }
}
