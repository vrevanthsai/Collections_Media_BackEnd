package com.manga.collectionBend.dto;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.entities.NotificationEntity;
import com.manga.collectionBend.utils.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Integer id;
    private NotificationType type;
    private Integer referenceId;
    private Integer sharesCount;
    private boolean isRead;
    private LocalDateTime createdAt;
    private Integer actorUserId;
    private String actorUsername;
    private String actorName;
    private String actorImageName;

    public static NotificationDto fromEntity(NotificationEntity notification) {
        UserEntity actor = notification.getActor();
        return NotificationDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .referenceId(notification.getReferenceId())
                .sharesCount(notification.getSharesCount()) // will be null value for all friend_connection types and only has count value for Collection Shared type notification
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .actorUserId(actor != null ? actor.getUserId() : null)
                .actorUsername(actor != null ? actor.getUniqueUsername() : null)
                .actorName(actor != null ? actor.getName() : null)
                .actorImageName(actor != null ? actor.getImageName() : null)
                .build();
    }
}
