package com.manga.collectionBend.dto;

import com.manga.collectionBend.utils.CollectionProgress;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
//@NoArgsConstructor
//@AllArgsConstructor
public class CollectionDto {

    public CollectionDto(Integer collectionId, String name, Integer category, Integer userId, String username, Integer rating, String review, String progress, CollectionProgress privacy, String addedDate, String imagename, String imageUrl) {
        this.collectionId = collectionId;
        this.name = name;
        this.category = category;
        this.userId = userId;
        this.username = username;
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
    private Integer category;

    // Used for GET responses- used in Collection-GET apis to send categoryName instead of categoryId to frontend
    private String categoryName;

    @NotBlank(message = "Please provide userId")
    private Integer userId;

    private String username;

    @NotBlank(message = "Please provide collection's rating")
    private Integer rating;

    @NotBlank(message = "Please provide collection's review")
    private String review;

    @NotBlank(message = "Please provide collection's progress")
    private String progress;

    @Enumerated(EnumType.STRING)
    private CollectionProgress privacy; // PUBLIC, PRIVATE, FRIENDS

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

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer categoryId) {
        this.category = categoryId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
