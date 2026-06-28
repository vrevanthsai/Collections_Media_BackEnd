package com.manga.collectionBend.auth.services;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.entities.UserRole;
import com.manga.collectionBend.auth.repositories.UserRepo;
import com.manga.collectionBend.auth.utils.AuthResponse;
import com.manga.collectionBend.auth.utils.LoginRequest;
import com.manga.collectionBend.auth.utils.RegisterRequest;
import com.manga.collectionBend.dto.ApiResponse;
import com.manga.collectionBend.entities.CategoryEntity;
import com.manga.collectionBend.exceptions.InvalidCredentialsException;
import com.manga.collectionBend.repositories.CategoryRepo;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final CategoryRepo categoryRepo;

//  Constructor Dependency Injection
    public AuthService(PasswordEncoder passwordEncoder, UserRepo userRepo, JwtService jwtService, RefreshTokenService refreshTokenService, AuthenticationManager authenticationManager, CategoryRepo categoryRepo) {
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
        this.categoryRepo = categoryRepo;
    }

//    Register Api service Method
    public ApiResponse<AuthResponse> register(RegisterRequest registerRequest){
//        Validation check to prevent users to create multiple accounts with same email-id
        Optional<UserEntity> existingUser = userRepo.findByEmail(registerRequest.getEmail());
        if(existingUser.isPresent()){
            return ApiResponse.error("Account already exists with email: " + registerRequest.getEmail());
        }

//      builder() is used to create objects step-by-step used for Very readable
//        or just use new UserEntity(registerRequest.getName(),...)-constructor to create objects Normally
//        registerRequest(is like DAO object of register) var has Client-input data coming from AuthController
        var user = UserEntity.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword())) // we encode the raw pwd from client and store that in DB
                .role(UserRole.USER) // by Default - All Nwe User-Registers will have Role as USER-later it will be changed to ADMIN- change it directly from MySql workbench or create a separate api
                .build();

//        Validation of user selected Categories initially while registering and min of 3 required
        if(registerRequest.getSelectedCategories() == null
                || registerRequest.getSelectedCategories().size() < 3) {

            throw new RuntimeException(
                    "Please select at least 3 categories"
            );
        }

//        Saving the Stored-Object into UserTable
        UserEntity savedUser = userRepo.save(user);

//        Save Categories After User Registration
        for(String categoryName : registerRequest.getSelectedCategories()) {

            CategoryEntity category = new CategoryEntity();

            category.setCategoryName(categoryName);
            category.setUser(savedUser);

            categoryRepo.save(category);
        }

//        generating Jwt Access token and RefreshToken after successfully saving data
        var accessToken = jwtService.generateToken(savedUser);
        var refreshToken = refreshTokenService.createRefreshToken(savedUser.getEmail()); // email is our Username-Security login field and it must be unique

//        linking the both tokens with AuthResponse
//        NOTE - TRY TO INCLUDE SOME User data fields also while sending Response from Service to Controller-API to Client(fetching this api)
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .userId(savedUser.getUserId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .username(savedUser.getUniqueUsername()) // sending user-entered username field data - not email
                .build();

        return ApiResponse.success(authResponse);
    }

//    Login Api Service Method
    public ApiResponse<AuthResponse> login(LoginRequest loginRequest){
//        this authenticates User-login credentials- if user is authenticated then only he can access our apis/services
        // this is linked with our jwtService and AuthFilterChain security mechanism
//        if provided credentials are correct then it will go to next step or returns 403 forbidden
//       logic to handle a custom exception message to user to tell provided credentials are incorrect
        try {
            // Throws BadCredentialsException if credentials are wrong
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid email or password. Please try again.");
        } catch (Exception ex) {
            throw new InvalidCredentialsException("Authentication failed. Please try again.");
        }

//        user var is of type - UserEntity
        var user = userRepo.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found with provided Email/Password")); // gets logged-In user data
//        if credentials are correct then generate accessToken and refreshToken
        var accessToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(loginRequest.getEmail());

        //        linking the both tokens with AuthResponse
//        NOTE - TRY TO INCLUDE SOME User data fields also while sending Response from Service to Controller-API to Client(fetching this api)
        AuthResponse authResponse =  AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .username(user.getUniqueUsername()) // sending user-entered username field data - not email
                .build();

        return ApiResponse.success(authResponse);
    }
}
