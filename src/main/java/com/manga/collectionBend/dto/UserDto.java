package com.manga.collectionBend.dto;

import com.manga.collectionBend.auth.entities.UserRole;
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
    private UserRole role;
    private boolean suspended;
    private String addedDate;
}
