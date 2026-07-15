package com.manga.collectionBend.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDto {
    private Integer userId;
    private String name;
    private String username;
    private String email;
    private String addedDate;
}
