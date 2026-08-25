package com.manga.collectionBend.controllers;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.dto.*;
import com.manga.collectionBend.service.ShareCollectionService;
import com.manga.collectionBend.utils.RecommendationsTabType;
import com.manga.collectionBend.utils.ShareActionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/user/{userId}/shares")
@RequiredArgsConstructor
public class ShareCollectionController {
    private final ShareCollectionService shareService;

    @PostMapping("/share-collection")
    public ApiResponse<ShareResultDto> shareCollections(
            @RequestBody ShareRequest request,
            @AuthenticationPrincipal UserEntity currentUser) {
        ShareResultDto result = shareService.shareCollections(
                request.getCollectionIds(), currentUser.getUserId(), request.getFriendUserIds());
        return ApiResponse.success(result);
    }

//    Not using this Api anymore - it's linked in /get-recommendations api below
//    Api which sends list/data about total shared Collections list done by A user to their friends till now
    @GetMapping("/shareby-me")
    public ApiResponse<List<SharedCollectionDto>> getSharedWithMe(@AuthenticationPrincipal UserEntity currentUser) {
        return ApiResponse.success(shareService.getSharedByMe(currentUser.getUserId()));
    }

    @PatchMapping("/{shareId}/viewed")
    public ApiResponse<String> markViewed(@PathVariable Integer shareId, @PathVariable Integer userId) { // userId from parent mapping
        shareService.markAsViewed(shareId, userId);
        return ApiResponse.success("Marked as viewed");
    }

    @PatchMapping("/{shareId}/action")
    public ApiResponse<String> updateAction(@PathVariable Integer shareId, @RequestParam ShareActionStatus status, @PathVariable Integer userId) {
        shareService.updateActionStatus(shareId, status, userId);
        return ApiResponse.success("Status updated");
    }

//    this api used for Shared Collections or Recommendations page to get total shared/recommended collections by multiple friend users or user recommended to his friends
    @GetMapping("/get-recommendations")
    public ApiResponse<List<GroupedShareDto>> getGroupedShares(@AuthenticationPrincipal UserEntity currentUser, @RequestParam RecommendationsTabType tabType) {
        return ApiResponse.success(shareService.getGroupedSharesFromFriends(currentUser.getUserId(), tabType));
    }

    @PatchMapping("/{shareId}/watch-list")
    public ApiResponse<String> markWatchList(@PathVariable Integer shareId, @RequestParam Boolean isWatchList, @PathVariable Integer userId) {
        shareService.markAsWatchList(shareId, isWatchList, userId);
        return ApiResponse.success("Shared/Recommended Collection watchlist status updated");
    }
}
