package com.ironcore.infrastructure.security.filter;

import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenClaims;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";

    private final JwtAccessTokenValidator validator;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getServletPath();

        return method.equals(HttpMethod.OPTIONS.name())
                || path.equals("/actuator/health")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || isPublicEndpoint(method, path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = recoveryToken(request);

        if (token != null) {
            JwtAccessTokenClaims claims = validator.validate(token);

            userRepository.findById(claims.userId())
                    .filter(User::isActive)
                    .ifPresent(user -> {
                        AuthenticatedUser principal = new AuthenticatedUser(
                                user.getId(), user.getEmail(), user.mustChangePassword()
                        );

                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of()
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }

        filterChain.doFilter(request, response);
    }

    private String recoveryToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private boolean isPublicEndpoint(String method, String path) {
        return method.equals(HttpMethod.POST.name())
                && (path.equals("/api/auth/login")
                || path.equals("/api/auth/logout")
                || path.equals("/api/users/change-initial-password"));
    }
}
