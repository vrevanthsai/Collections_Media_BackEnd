package com.manga.collectionBend.dto;

import com.manga.collectionBend.entities.SharedCollection;
import com.manga.collectionBend.utils.ShareActionStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedCollectionDto {
    private Integer shareId;
    private Integer collectionId;
    private String collectionName;
//    private String imageName;
    private String categoryName;
    private String sharedByUsername;
    private LocalDateTime sharedAt;
    private boolean isViewed; // is- work is removed from var-name only sends - viewed var-name to frondend- response JSON
    private ShareActionStatus actionStatus;
    private boolean isAddedToWatchlist;

    public static SharedCollectionDto fromEntity(SharedCollection share) {
        return SharedCollectionDto.builder()
                .shareId(share.getId())
                .collectionId(share.getCollection().getCollectionId())
                .collectionName(share.getCollection().getName())
//                .imageName(share.getCollection().getImagename())
                .categoryName(share.getCollection().getCategory().getCategoryName())
                .sharedByUsername(share.getSharedBy().getUniqueUsername())
                .sharedAt(share.getSharedAt())
                .isViewed(share.isViewed())
                .isAddedToWatchlist(share.isAddedToWatchlist())
                .actionStatus(share.getActionStatus())
                .build();
    }
}
