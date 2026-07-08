package com.manga.collectionBend.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProfileRequest {
    private Integer userId; // not editable
    private String email; // not editable
    private String name;
    private String username;
}
