package com.manga.collectionBend.auth.services;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// This file used for Validating further Api requests which contains valid Access Tokens and
// Act as security Filter layer before accessing CRUD Apis and we use OncePerRequestFilter abstract class to implement this process
@Service
public class AuthFilterService  extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    public AuthFilterService(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
//   Client passes the AccessToken through Header(Authorization) of Api request
//   now extracting Token details from request
        final String authHeader = request.getHeader("Authorization");
        String jwt;
        String username;

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
//           it will pass to next filter and there token is required - so it will reject the request and returns Bad reuest
            filterChain.doFilter(request, response);
            return;
        }
//        if token exist then extract it
        jwt = authHeader.substring(7); // 7 is the index value where Token starts after "Bearer Example_Token"

//        extract username from JWT
        username = jwtService.extractUsername(jwt);

//        username should not be null and user is not Authenticated yet
//        SecurityContextHolder holders the context/data of User Authentication
        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = userDetailsService.loadUserByUsername(username); // here username value is email
//            these 2 vars are used for this isTokenValid() method to check Validity of token with respect to user data
            if(jwtService.isTokenValid(jwt, userDetails)){
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, // here itself user credentials are passed, so next arg value will be null
                        null,
                        userDetails.getAuthorities() // this will tell what permission does This user have like USER or Admin
                );

//                setting user details to authenticated token
                authenticationToken.setDetails(
//          WebAuthenticationDetailsSource which builds the details object from an HttpServletRequest object then creates a WebAuthenticationDetails object
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

//                Now User is Authenticated after validating his Access token
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

//        Now after validation completes - the flow should go to next filters in Chain and next filter layers know that this user-request is Authenticated
        filterChain.doFilter(request, response);
    }
}
