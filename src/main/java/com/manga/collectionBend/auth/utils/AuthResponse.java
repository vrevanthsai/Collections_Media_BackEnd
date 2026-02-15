package com.manga.collectionBend.auth.utils;

// THis file give Common-Response for other Auth-Request files in this utils package
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

//    User Data fields to send to frontend
    private String name;
    private String username;
    private String email;

//        NOTE - TRY TO INCLUDE SOME User data fields also while sending Response from Service to Controller-API to Client(fetching this api)


    public AuthResponse(String accessToken, String refreshToken, String name, String username, String email) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.name = name;
        this.username = username;
        this.email = email;
    }

    public AuthResponse(){}

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /* =======================
       Builder- manual implementation because lombok- @Builder is not working
       - builder() is used to create objects step-by-step used for Very readable
       - or just use new ClassName()-constructor to create objects Normally
       - lombok methods works when running server but does not work while writing code(in IDE-suggestions)
       ======================= */
    
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String accessToken;
        private String refreshToken;
        private String name;
        private String username;
        private String email;


        private Builder() {
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public AuthResponse build() {
            AuthResponse response = new AuthResponse();
            response.accessToken = this.accessToken;
            response.refreshToken = this.refreshToken;
            response.name = this.name;
            response.username = this.username;
            response.email = this.email;
            return response;
        }
    }
}
