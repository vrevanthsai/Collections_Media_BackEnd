package com.manga.collectionBend.dto;

import com.manga.collectionBend.auth.entities.UserEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendDto {
    private Integer userId;
    private String username;
    private String name;
    private String imageName;
    private String addedDate;

//    we build the FriendDto linking UserEntity build here itself instead of declaring them in service file
    public static FriendDto fromEntity(UserEntity user) {
        return FriendDto.builder()
                .userId(user.getUserId())
                .username(user.getUniqueUsername())
                .name(user.getName())
                .imageName(user.getImageName())
                .addedDate(user.getAddedDate())
                .build();
    }
}
