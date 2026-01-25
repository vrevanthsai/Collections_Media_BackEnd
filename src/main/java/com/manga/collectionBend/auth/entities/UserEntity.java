package com.manga.collectionBend.auth.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
// UserDetails is used for integrating this userEntity class with spring security for login filter
public class UserEntity implements UserDetails {
    public UserEntity(Integer userId, String name, String username, String email, String password, RefreshToken refreshToken, UserRole role,
                      boolean isEnabled, boolean isAccountNonExpired, boolean isAccountNonLocked,
                      boolean isCredentialsNonExpired) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.refreshToken = refreshToken;
        this.role = role;
        this.isEnabled = isEnabled;
        this.isAccountNonExpired = isAccountNonExpired;
        this.isAccountNonLocked = isAccountNonLocked;
        this.isCredentialsNonExpired = isCredentialsNonExpired;
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

//    which handles enum values
    @Enumerated(EnumType.STRING)
    private UserRole role;

//    vars for abstract methods and by default user will not login by marking all as 'true'
    private boolean isEnabled = true;

    private boolean isAccountNonExpired = true;

    private boolean isAccountNonLocked = true;

    private boolean isCredentialsNonExpired = true;

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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
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

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
