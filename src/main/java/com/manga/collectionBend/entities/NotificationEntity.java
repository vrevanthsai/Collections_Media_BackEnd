package com.manga.collectionBend.entities;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.utils.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private UserEntity recipient; // here recipient gets the Notification msg in Frontend caused by actor with Type as reason

    // who triggered this notification (e.g. the friend requester) — useful for showing "X sent you a request"
    @ManyToOne
    @JoinColumn(name = "actor_id", nullable = false)
    private UserEntity actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    // points to the relevant entity (FriendConnection id, Shared_Collection id, etc.) depending on `type`
    private Integer referenceId;

    // shared collection count, meaningful only for certain types (e.g. COLLECTION_SHARED)
    private Integer sharesCount;

    // collection data- denormalized snapshot fields, populated at notification creation time to show in frontend and navigations for LIKED, ADD_to_watch actionStatus types of SharedCollection records done from frontend
//     this 2 vars/column are not used for FriendConnection types - only used for Collection_Liked types - not even used for Collection_shared also
    private Integer collectionId;      // used for frontend navigation: /collection/{collectionId}
    private String collectionName;     // shown directly in notification text

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}