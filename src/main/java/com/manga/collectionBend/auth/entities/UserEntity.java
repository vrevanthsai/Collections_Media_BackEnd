package com.manga.collectionBend.auth.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
// UserDetails is used for integrating this userEntity class with spring security for login filter
public class UserEntity implements UserDetails {
    public UserEntity(Integer userId, String name, String username, String email,
                      String password, RefreshToken refreshToken, UserRole role) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.refreshToken = refreshToken;
        this.role = role;
    }

    public UserEntity(){}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @NotBlank(message = "The name field can't be blank")
    private String name;

//    this var contains unique naming value - only one unique name exists for each user
    @NotBlank(message = "The username field can't be blank")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "The email field can't be blank")
    @Column(unique = true)
//    this @Email does auto validation - so no rxjs or other spring patterns are needed
    @Email(message = "Please enter valid email")
    private String email;

    @NotBlank(message = "The password field can't be blank")
    @Size(min = 5, message = "The password must have at least 5 characters")
    private String password;

//     we store refresh tokens in DB of user table to avoid multiple user re-logins for some duration
//    onetoone is for mapping two tables with each other
//    "user" is var name from other table-from RefreshToken- both must have same name
    @OneToOne(mappedBy = "user")
    private RefreshToken refreshToken;

//    mapping ForgotPassword entity with User entity(Parent-Table)
//    @OneToOne(mappedBy = "user")
//    private ForgotPassword forgotPassword;

//   ForgotPassword field/column data should be deleted automatically from Users-table when that user resets password
    @OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(name = "forgot_password_id")
    private ForgotPassword forgotPassword;

//    which handles enum values
    @Enumerated(EnumType.STRING)
    private UserRole role;

    public Integer getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public RefreshToken getRefreshToken() {
        return refreshToken;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRefreshToken(RefreshToken refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    //    Abstract Methods of UserDetails Interface
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
//        used for checking authorities based on given UserRole Enum values
//        used for spring security identifying the permissions
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

//    this getter method is from interface
    @Override
    public String getUsername() {
        return email; // or username
    }

    //  return value for abstract methods will be true and by default user will not login by marking all as 'true'
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /* =======================
       Builder- manual implementation because lombok- @Builder is not working
       - lombok methods works when running server but does not work while writing code(in IDE-suggestions)
       ======================= */

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Integer userId;
        private String name;
        private String email;
        private String username;
        private String password;
        private RefreshToken refreshToken;
        private UserRole role;

        private Builder() {
        }

        public Builder userId(Integer userId) {
            this.userId = userId;
            return this;
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

        public Builder refreshToken(RefreshToken refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder role(UserRole role) {
            this.role = role;
            return this;
        }

        public UserEntity build() {
            UserEntity user = new UserEntity();
            user.userId = this.userId;
            user.name = this.name;
            user.email = this.email;
            user.username = this.username;
            user.password = this.password;
            user.refreshToken = this.refreshToken;
            user.role = this.role;
            return user;
        }
    }
}
