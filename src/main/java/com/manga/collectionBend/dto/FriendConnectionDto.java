package com.manga.collectionBend.dto;

import com.manga.collectionBend.utils.FriendStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FriendConnectionDto {
    private Integer connectionId;
    private Integer requesterId;
    private Integer receiverId;
    private FriendStatus status;
}
