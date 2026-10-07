package com.manga.collectionBend.entities;

import com.manga.collectionBend.auth.entities.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "category_name"}) // composite uniqueness (unique per user, not globally)
})
@Getter
@Setter
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;

    // now nullable — only set for CUSTOM (user-created) categories
    // for default-sourced categories, this stays null; name comes from defaultCategory instead
    @Column(nullable = true)
    private String categoryName;

    // CategoryEntity — owning side (has the actual @JoinColumn/FK)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // links back to the admin-managed default, if this category originated from one
    // null means this is a fully custom, user-created category
    @ManyToOne
    @JoinColumn(name = "default_category_id", nullable = true)
    private DefaultCategoryEntity defaultCategory;

    @Column(nullable = false)
    private boolean isEditable = true; // false for default-sourced categories, true for custom ones

    //    THis var are only used for Auto- syncing both parent and child tables which is used for Auto-Deletion
//    so new column creation will happen in this MySql DB table
    // here userId is the var name used in CollectionEntity as mapped to this CategoryEntity- both must have same name
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectionEntity> collections = new ArrayList<>();

    // single source of truth for "what name should the frontend show"
    // reads live from defaultCategory if this is default-sourced, otherwise uses the stored custom name
//    instead of calling actual categoryName which will have null for default-categories - we use this method which conditionally sends correct categoryName from default-category list instead of null
    @Transient
    public String getEffectiveCategoryName() {
        return defaultCategory != null ? defaultCategory.getCategoryName() : categoryName;
    }
}
