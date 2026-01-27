package com.manga.collectionBend.auth.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
//@Builder // creates builder methods
//@NoArgsConstructor
//@AllArgsConstructor
public class RefreshToken {

    public RefreshToken(Integer tokenId, String refreshToken, Instant expirationTime, UserEntity user) {
        this.tokenId = tokenId;
        this.refreshToken = refreshToken;
        this.expirationTime = expirationTime;
        this.user = user;
    }

    public RefreshToken() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tokenId;

    @Column(nullable = false, length = 500) // even with large token characters - table will accept them
    @NotBlank(message = "Please enter refresh token value!")
    private String refreshToken;

    @Column(nullable = false)
    private Instant expirationTime;

//    onetoone is for mapping two tables with each other
//    defines a relationship where one instance of an entity is associated with exactly one instance of another entity.
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false) // to map both tables properly and user_id(primaryKey)  is default naming
    private UserEntity user;

    public Integer getTokenId() {
        return tokenId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    public UserEntity getUser() {
        return user;
    }

    /* =======================
           Builder implementation from ChatGpt
           ======================= */
// using manual builder() method logic because lombok is not working - lombok methods works when running server but does not work while writing code(in IDE-suggestions)
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Integer tokenId;
        private String refreshToken;
        private Instant expirationTime;
        private UserEntity user;

        private Builder() {
        }

        public Builder tokenId(Integer tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder expirationTime(Instant expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        public Builder user(UserEntity user) {
            this.user = user;
            return this;
        }

        public RefreshToken build() {
            RefreshToken token = new RefreshToken();
            token.tokenId = this.tokenId;
            token.refreshToken = this.refreshToken;
            token.expirationTime = this.expirationTime;
            token.user = this.user;
            return token;
        }
    }

}
