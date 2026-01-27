package com.manga.collectionBend.auth.utils;

// this file contains Login API logic and what login-Request body looks like and this file is like DAO
public class LoginRequest {

    private String email;
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public LoginRequest(){}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

        private String email;
        private String password;

        private Builder() {
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public LoginRequest build() {
            LoginRequest request = new LoginRequest();
            request.email = this.email;
            request.password = this.password;
            return request;
        }
    }
}
