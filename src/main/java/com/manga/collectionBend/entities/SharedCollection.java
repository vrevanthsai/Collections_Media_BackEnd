package com.manga.collectionBend.entities;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.utils.ShareActionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shared_collections", indexes = {
        @Index(name = "idx_shared_with", columnList = "shared_with_id"),
        @Index(name = "idx_shared_by", columnList = "shared_by_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "collection_id", nullable = false)
    private CollectionEntity collection; // the original collection being shared

    @ManyToOne
    @JoinColumn(name = "shared_by_id", nullable = false)
    private UserEntity sharedBy; // who shared it

    @ManyToOne
    @JoinColumn(name = "shared_with_id", nullable = false)
    private UserEntity sharedWith; // who received it

    @Column(nullable = false)
    private LocalDateTime sharedAt;

    @Column(nullable = false)
    private boolean isViewed = false; // did the recipient open/see this share yet

    // did the recipient act on it — e.g. "whether he liked it or added to his watch list or dismissed"
    @Enumerated(EnumType.STRING)
    private ShareActionStatus actionStatus; // PENDING, ADD_TO_WATCH_LIST, LIKED, DISMISSED
}
