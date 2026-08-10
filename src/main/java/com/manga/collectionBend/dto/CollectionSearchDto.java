package com.manga.collectionBend.dto;

import com.manga.collectionBend.entities.CollectionEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionSearchDto {
    private Integer collectionId;
    private String collectionName;
    private String imageName;

    public static CollectionSearchDto fromEntity(CollectionEntity collection) {
        return CollectionSearchDto.builder()
                .collectionId(collection.getCollectionId())
                .collectionName(collection.getName())
                .imageName(collection.getImagename())
                .build();
    }
}
