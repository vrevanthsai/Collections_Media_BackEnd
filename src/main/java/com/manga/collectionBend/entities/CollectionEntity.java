package com.manga.collectionBend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
//@NoArgsConstructor
//@AllArgsConstructor
//@Getter
@Table(name = "collection")
public class CollectionEntity {

    public CollectionEntity(Integer collectionId, String name, String category, String userId, Integer rating, String review, String progress, String privacy, String addedDate, String imagename) {
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

    @Column(nullable = false, length = 300)
    @NotBlank(message = "Please provide collection's name")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "Please provide collection's category")
    private String category;

    @Column(nullable = false)
    @NotBlank(message = "Please provide userId")
    private String userId;

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
    @NotBlank(message = "Please provide collection's privacy")
    private String privacy;

    @Column(nullable = false)
    @NotBlank(message = "Please provide collection's created Date")
//    private Date addedDate;
    private String addedDate;

    @Column(nullable = false, length = 300)
    @NotBlank(message = "Please provide collection's imagename")
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
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
