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
}
