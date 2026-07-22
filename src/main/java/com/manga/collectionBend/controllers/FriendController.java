package com.manga.collectionBend.controllers;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.dto.ApiResponse;
import com.manga.collectionBend.dto.FriendDto;
import com.manga.collectionBend.service.FriendService;
import com.manga.collectionBend.utils.FriendStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/{userId}/friends") // currentUser's userId(who is sending api request)
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

//    Send Friend Request to other user Api
    @PostMapping("/request/{receiverId}")
//    here @AuthenticationPrincipal is used to get user data who is requesting this api and send that user's userId to service method
//    instead of @AuthenticationPrincipal- we can use @RequestParam and get userId from parent-mapping
    public ResponseEntity<ApiResponse<String>> sendRequest(@PathVariable Integer receiverId, @AuthenticationPrincipal UserEntity currentUser) {
        ApiResponse<String> response = friendService.sendFriendRequest(currentUser.getUserId(), receiverId);
        //        send success=false and error msg with Conflict status code- 409 - when any error res comes from service-method
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
//        send success=true, with CollectionDto data object when no errors are there
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/respond/{connectionId}")
//    here you can use @PathVariable also - instead of @RequestParam - to get a single word input from frontend
    public ApiResponse<String> respond(@PathVariable Integer connectionId, @RequestParam FriendStatus action, @PathVariable Integer userId) { // userId from parent-mapping path
        friendService.respondToRequest(connectionId, action, userId); // action = ACCEPTED or REJECTED
        return ApiResponse.success("Request " + action.name().toLowerCase());
    }

    @GetMapping("/get-all-friends-list")
    public ApiResponse<List<FriendDto>> getFriends(@PathVariable Integer userId) { // from parent-mapping
        return ApiResponse.success(friendService.getFriends(userId)); // currentUserId
    }

    @DeleteMapping("/unfriend/{otherUserId}")
    public ApiResponse<String> unfriend(@PathVariable Integer otherUserId, @AuthenticationPrincipal UserEntity currentUser) {
        friendService.unfriend(currentUser.getUserId(), otherUserId);
        return ApiResponse.success("Unfriended successfully");
    }

    @PostMapping("/block/{targetUserId}")
    public ApiResponse<String> block(@PathVariable Integer targetUserId, @AuthenticationPrincipal UserEntity currentUser) {
        friendService.blockUser(currentUser.getUserId(), targetUserId);
        return ApiResponse.success("User blocked");
    }

    @PostMapping("/unblock/{targetUserId}")
    public ApiResponse<String> unblock(@PathVariable Integer targetUserId, @AuthenticationPrincipal UserEntity currentUser) {
        friendService.unblockUser(currentUser.getUserId(), targetUserId);
        return ApiResponse.success("User unblocked");
    }

    @GetMapping("/get-blocked-users-list")
//    this api sends list of blocked users list done by a User(currentUser)
    public ApiResponse<List<FriendDto>> getBlocked(@AuthenticationPrincipal UserEntity currentUser) {
        return ApiResponse.success(friendService.getBlockedUsers(currentUser.getUserId()));
    }
}
