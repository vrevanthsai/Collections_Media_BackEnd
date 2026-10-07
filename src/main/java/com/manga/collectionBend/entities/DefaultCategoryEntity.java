package com.manga.collectionBend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "default_categories")
@Getter
@Setter
//this table is managed by Admin- only Admin can add or edit but Admin cant delete created default-categories- because they will be linked to User based categories(separate table)- so only Edit is possible after a default-category is created by Admin
public class DefaultCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true) // fine here — this table has no per-user concept
    private String categoryName;

    @Column(nullable = false)
    private boolean active = true; // lets admin "retire" a default without deleting history

    private String iconName; // optional — if you want icons per default category later
}
