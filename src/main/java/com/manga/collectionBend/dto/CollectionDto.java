package com.manga.collectionBend.dto;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.entities.CategoryEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
//@NoArgsConstructor
//@AllArgsConstructor
public class CollectionDto {

    public CollectionDto(Integer collectionId, String name, @NotBlank CategoryEntity category, @NotBlank UserEntity userId, Integer rating, String review, String progress, String privacy, String addedDate, String imagename, String imageUrl) {
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
        this.imageUrl = imageUrl;
    }

    public CollectionDto() {
    }

    private Integer collectionId;

    @NotBlank(message = "Please provide collection's name")
    private String name;

    @NotBlank(message = "Please provide collection's category")
    private CategoryEntity category;

    @NotBlank(message = "Please provide userId")
    private UserEntity userId;

    @NotBlank(message = "Please provide collection's rating")
    private Integer rating;

    @NotBlank(message = "Please provide collection's review")
    private String review;

    @NotBlank(message = "Please provide collection's progress")
    private String progress;

    @NotBlank(message = "Please provide collection's privacy")
    private String privacy;

    @NotBlank(message = "Please provide collection's created Date")
//    private Date addedDate;
    private String addedDate;

    @NotBlank(message = "Please provide collection's imagename")
    private String imagename;

    @NotBlank(message = "Please provide collection's ImageURL")
    private String imageUrl;

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

    public void setUserId(UserEntity userId) {
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
