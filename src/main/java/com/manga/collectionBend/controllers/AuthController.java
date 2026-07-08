package com.manga.collectionBend.controllers;


import com.manga.collectionBend.auth.entities.RefreshToken;
import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.services.AuthService;
import com.manga.collectionBend.auth.services.JwtService;
import com.manga.collectionBend.auth.services.RefreshTokenService;
import com.manga.collectionBend.auth.utils.AuthResponse;
import com.manga.collectionBend.auth.utils.LoginRequest;
import com.manga.collectionBend.auth.utils.RefreshTokenRequest;
import com.manga.collectionBend.auth.utils.RegisterRequest;
import com.manga.collectionBend.dto.ApiResponse;
import com.manga.collectionBend.dto.CategoryDto;
import com.manga.collectionBend.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// this file contains all Auth-APIs endpoints
@RestController
@CrossOrigin(origins = "*") // to allow other sources to access this controller APIs and * is allow all
@RequestMapping("/api/v1/auth/") // same base path should be in SecurityConfig file
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final CategoryService categoryService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService, JwtService jwtService, CategoryService categoryService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
        this.categoryService = categoryService;
    }

//    Register API
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest registerRequest){
        ApiResponse<AuthResponse> response = authService.register(registerRequest);
        //   send success=false and error msg with Conflict status code- 409 - when any error res comes from service-method
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
//        send success=true, with AuthResponse data object when no errors are there
        return ResponseEntity.ok(response);
    }

    //    Login API
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest loginRequest){ // or create and use normal LoginDao class for POJOs
        ApiResponse<AuthResponse> response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

//    RefreshToken Handling Api which generates new accessTokens when user Relogins(with Token) without credentials
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest){
//     when existing users try to login - then we verify user's existing refreshToken(which frontend sends from browser-localStorage/Session/Cookies)
//       and after verifying - we send new Access Token to user which allows user to access our Apis without Login-credentials
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenRequest.getRefreshToken());
        UserEntity user = refreshToken.getUser();

//        generating new AccessToken based on given verified refreshToken
        String accessToken = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken()) // sending provided verified refreshToken - not new one
                .build()
        );
    }

    //    this Api endpoint does not require security- so excluded it in SecurityConfig
//    added this Category Api - here because it does not require any Security filters and is used in /register form in forntend
    @GetMapping("/get-default-categories")
    public ResponseEntity<List<CategoryDto>> getDefaultCategories() {
        return ResponseEntity.ok(
                categoryService.getDefaultCategories()
        );
    }

}
