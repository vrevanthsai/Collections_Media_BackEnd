package com.manga.collectionBend.entities;

import com.manga.collectionBend.auth.entities.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;

    @Column(nullable = false)
    private String categoryName;

    // CategoryEntity — owning side (has the actual @JoinColumn/FK)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    //    THis var are only used for Auto- syncing both parent and child tables which is used for Auto-Deletion
//    so new column creation will happen in this MySql DB table
    // here userId is the var name used in CollectionEntity as mapped to this CategoryEntity- both must have same name
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectionEntity> collections = new ArrayList<>();
}
