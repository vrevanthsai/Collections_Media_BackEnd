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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// this file contains all Auth-APIs endpoints
@RestController
@RequestMapping("/api/v1/auth/") // same base path should be in SecurityConfig file
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService, JwtService jwtService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

//    Register API
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest){
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    //    Login API
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest){ // or create and use normal LoginDao class for POJOs
        return ResponseEntity.ok(authService.login(loginRequest));
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
}
