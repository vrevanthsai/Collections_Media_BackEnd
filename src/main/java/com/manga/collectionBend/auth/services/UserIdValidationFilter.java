package com.manga.collectionBend.auth.services;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserIdValidationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public UserIdValidationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    private static final List<String> EXEMPT_PATTERNS = List.of(
            "/api/v1/auth/", "/forgotPassword/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1. Skip exempt paths (login, register, forgotPassword etc.)
        if (EXEMPT_PATTERNS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract JWT from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // no token — let Spring Security handle 401
            return;
        }

        // 3. Extract userId from JWT claim
        String token = authHeader.substring(7);
        String jwtUserId;
        try {
            jwtUserId = jwtService.extractClaim(token, claims -> {
                Object id = claims.get("userId");
                return id != null ? String.valueOf(id) : null;
            });
        } catch (Exception e) {
            filterChain.doFilter(request, response); // invalid token — let AuthFilterService handle it
            return;
        }

        // 4. If no userId claim found in token, skip validation
        if (jwtUserId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Extract userId from the incoming request (path variable or query param)
        String requestUserId = extractUserIdFromRequest(request);

        // 6. If no userId found in request, no validation needed (endpoint doesn't use userId)
        if (requestUserId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 7. Compare — block if they don't match
        if (!requestUserId.equals(jwtUserId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\": \"Access denied\", \"message\": \"You are not authorized to access another user's data\"}"
            );
            return; // do NOT call filterChain — request is blocked
        }

        // 8. IDs match — allow request through
        filterChain.doFilter(request, response);
    }

    private String extractUserIdFromRequest(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Check path variable: /api/users/{userId}/... or /api/{userId}/...
        // Matches any numeric or UUID segment after /users/ or similar
        Pattern pattern = Pattern.compile("/(?:users|user)/([a-zA-Z0-9\\-]+)");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // Check query param: ?userId=123
        String paramUserId = request.getParameter("userId");
        if (paramUserId != null && !paramUserId.isBlank()) {
            return paramUserId;
        }

        return null; // no userId found in this request
    }
}
