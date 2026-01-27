package com.manga.collectionBend.auth.services;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.entities.UserRole;
import com.manga.collectionBend.auth.repositories.UserRepo;
import com.manga.collectionBend.auth.utils.AuthResponse;
import com.manga.collectionBend.auth.utils.LoginRequest;
import com.manga.collectionBend.auth.utils.RegisterRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

//  Constructor Dependency Injection
    public AuthService(PasswordEncoder passwordEncoder, UserRepo userRepo, JwtService jwtService, RefreshTokenService refreshTokenService, AuthenticationManager authenticationManager) {
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
    }

//    Register Api service Method
    public AuthResponse register(RegisterRequest registerRequest){
//      builder() is used to create objects step-by-step used for Very readable
//        or just use new UserEntity(registerRequest.getName(),...)-constructor to create objects Normally
//        registerRequest(is like DAO object of register) var has Client-input data coming from AuthController
        var user = UserEntity.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword())) // we encode the raw pwd from client and store that in DB
                .role(UserRole.USER) // by Default - All Nwe User-Registers will have Role as USER-later it will be changed to Admin
                .build();

//        Saving the Stored-Object into UserTable
        UserEntity savedUser = userRepo.save(user);

//        generating Jwt Access token and RefreshToken after successfully saving data
        var accessToken = jwtService.generateToken(savedUser);
        var refreshToken = refreshTokenService.createRefreshToken(savedUser.getEmail()); // email is our Username-Security login field and it must be unique

//        linking the both tokens with AuthResponse
//        NOTE - TRY TO INCLUDE SOME User data fields also while sending Response from Service to Controller-API to Client(fetching this api)
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .build();
    }

//    Login Api Service Method
    public AuthResponse login(LoginRequest loginRequest){
//        this authenticates User-login credentials- if user is authenticated then only he can access our apis/services
        authenticationManager.authenticate(  // this is linked with our jwtService and AuthFilterChain security mechanism
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        var user = userRepo.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found with provided Email/Password")); // gets logged-In user data
//        if credentials are correct then generate accessToken and refreshToken
        var accessToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(loginRequest.getEmail());

        //        linking the both tokens with AuthResponse
//        NOTE - TRY TO INCLUDE SOME User data fields also while sending Response from Service to Controller-API to Client(fetching this api)
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .build();
    }
}
