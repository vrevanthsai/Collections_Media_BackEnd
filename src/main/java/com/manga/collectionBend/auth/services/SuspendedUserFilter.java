package com.manga.collectionBend.auth.services;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.repositories.UserRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SuspendedUserFilter extends OncePerRequestFilter {

    private final UserRepo userRepo;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/v1/auth/",   // covers login, register, activate, refresh-token etc.
            "/forgotPassword/"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // if no authenticated user (e.g. permitAll paths like login/register), just continue
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            filterChain.doFilter(request, response);
            return;
        }

//         here authentication.getPrincipal()- means it has either out user's email or some token which automatically gives userEntity data/instance
        if (authentication.getPrincipal() instanceof UserEntity user) {
            if (user.isSuspended()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                response.setContentType("application/json");
                response.getWriter().write("""
                    {
                      "success": false,
                      "message": "Your account has been suspended. Please contact support."
                    }
                    """);
                return; // stop the filter chain — request never reaches the controller
            }
        }

        filterChain.doFilter(request, response);
    }
}