package com.manga.collectionBend.auth.utils;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// this file contains Register API logic and what Register-Request body looks like and this file is like DAO
public class RegisterRequest {
    
    private String name;
    private String email;
    private String username;
    @NotBlank(message = "The password field can't be blank")
    @Size(min = 5, message = "The password must have at least 5 characters")
    private String password;

    public RegisterRequest(String name, String email, String username, String password) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
    }
    
    public RegisterRequest(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

        private String name;
        private String email;
        private String username;
        private String password;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public RegisterRequest build() {
            RegisterRequest request = new RegisterRequest();
            request.name = this.name;
            request.email = this.email;
            request.username = this.username;
            request.password = this.password;
            return request;
        }
    }
}
