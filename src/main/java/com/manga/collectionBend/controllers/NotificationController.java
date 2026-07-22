package com.manga.collectionBend.controllers;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.dto.ApiResponse;
import com.manga.collectionBend.dto.NotificationDto;
import com.manga.collectionBend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/{userId}/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/get-all-notifications")
    //    here @AuthenticationPrincipal is used to get user data who is requesting this api and send that user's userId to service method
//    instead of @AuthenticationPrincipal- we can use @RequestParam and get userId from parent-mapping
    public ApiResponse<List<NotificationDto>> getAll(@AuthenticationPrincipal UserEntity currentUser) {
        return ApiResponse.success(notificationService.getAllNotifications(currentUser.getUserId()));
    }

    @GetMapping("/unread")
    public ApiResponse<List<NotificationDto>> getUnread(@PathVariable Integer userId) {
        return ApiResponse.success(notificationService.getUnreadNotifications(userId));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(@AuthenticationPrincipal UserEntity currentUser) {
        return ApiResponse.success(notificationService.getUnreadCount(currentUser.getUserId()));
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<String> markAsRead(@PathVariable Integer notificationId) {
        notificationService.markAsRead(notificationId);
        return ApiResponse.success("Marked as read");
    }

    @PatchMapping("/read-all")
    public ApiResponse<String> markAllAsRead(@PathVariable Integer userId) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.success("All notifications marked as read");
    }

    @DeleteMapping("/delete-notification/{notificationId}")
    public ApiResponse<String> deleteNotification(@PathVariable Integer notificationId, @PathVariable Integer userId) {
        notificationService.deleteNotificationHandler(notificationId, userId);
        return ApiResponse.success("Notification deleted");
    }
}