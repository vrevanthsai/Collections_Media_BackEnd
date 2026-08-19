package com.manga.collectionBend.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupedShareDto {

    private Integer sharedByUserId;     // id of the friend who shared these collections
    private String sharedByUsername;    // username of the sharer, shown in notification/list UI
    private String sharedByImageName;   // sharer's avatar, for display next to the group

    private int collectionCount;        // how many collections this friend shared (for "shared 3 collections" text)
    private LocalDateTime latestSharedAt; // most recent share timestamp in this group, used for sorting/display

    private List<SharedCollectionDto> collections; // the actual shared collections, expandable in UI
}
