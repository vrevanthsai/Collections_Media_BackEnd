package com.manga.collectionBend.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareResultDto {
    private int totalSharesCreated;
    private List<String> skippedOrPartialRecipients; // usernames that were skipped or partially fulfilled
    private List<String> alreadySharedMessages; // to store already shared collections with friends, like e.g. "\"One Piece\" was already shared with Alex"
}
