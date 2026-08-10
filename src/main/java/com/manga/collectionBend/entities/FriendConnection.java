package com.manga.collectionBend.entities;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.utils.FriendStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "friend_connections", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"requester_id", "receiver_id"})
})
@Getter
@Setter
public class FriendConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private UserEntity requester; // the one who SENT the request

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver; // the one who RECEIVED the request

    @Enumerated(EnumType.STRING)
    private FriendStatus status; // PENDING, ACCEPTED, REJECTED, BLOCKED

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt; // when accepted/rejected/blocked
}
