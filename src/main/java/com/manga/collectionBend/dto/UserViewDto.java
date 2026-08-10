package com.manga.collectionBend.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserViewDto {
    private FriendDto user;
    private List<CollectionDto> collections;
}
