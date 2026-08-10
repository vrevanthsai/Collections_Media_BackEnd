package com.manga.collectionBend.entities;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.utils.CollectionProgress;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
//@NoArgsConstructor
//@AllArgsConstructor
//@Getter
@Table(name = "collection")
public class CollectionEntity {

    public CollectionEntity(Integer collectionId, String name, CategoryEntity category, UserEntity userId, Integer rating, String review, String progress, CollectionProgress privacy, String addedDate, String imagename) {
        this.collectionId = collectionId;
        this.name = name;
        this.category = category;
        this.userId = userId;
        this.rating = rating;
        this.review = review;
        this.progress = progress;
        this.privacy = privacy;
        this.addedDate = addedDate;
        this.imagename = imagename;
    }

    public CollectionEntity() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer collectionId;

    //    this var/column contains unique naming value - only one unique name exists for each collection to prevent duplicate entries
    @Column(nullable = false, length = 300, unique = true)
    @NotBlank(message = "Please provide collection's name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userId;

    @Column(nullable = false)
    @NotNull(message = "Please provide collection's rating")
    @Min(1)
    @Max(5)
    @Positive
    private Integer rating;

    @Column(nullable = false, length = 300)
    @NotBlank(message = "Please provide collection's review")
    private String review;

    @Column(nullable = false)
    @NotBlank(message = "Please provide collection's progress")
    private String progress;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CollectionProgress privacy; // PUBLIC, PRIVATE, FRIENDS

    @Column(nullable = false)
    @NotBlank(message = "Please provide collection's created Date")
//    private Date addedDate;
    private String addedDate;

    @Column(nullable = true, length = 300)
//    @NotBlank(message = "Please provide collection's imagename")
    private String imagename;

    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public UserEntity getUserId() {
        return userId;
    }

    public void setUserId(UserEntity user) {
        this.userId = user;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public CollectionProgress getPrivacy() {
        return privacy;
    }

    public void setPrivacy(CollectionProgress privacy) {
        this.privacy = privacy;
    }

    public String getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(String addedDate) {
        this.addedDate = addedDate;
    }

    public String getImagename() {
        return imagename;
    }

    public void setImagename(String imagename) {
        this.imagename = imagename;
    }
}
