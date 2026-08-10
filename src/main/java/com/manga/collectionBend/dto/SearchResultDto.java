package com.manga.collectionBend.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResultDto {
    private List<FriendDto> users;
    private List<CollectionSearchDto> collections;
}