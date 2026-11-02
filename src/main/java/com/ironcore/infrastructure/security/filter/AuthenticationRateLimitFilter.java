package com.ironcore.infrastructure.security.filter;

import com.ironcore.application.auth.service.AuthenticationAttemptLimiter;
import com.ironcore.application.exception.AuthenticationRateLimitException;
import com.ironcore.infrastructure.exception.PersistenceException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Set;

@RequiredArgsConstructor
public class AuthenticationRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> PATHS = Set.of("/api/auth/login", "/api/auth/refresh",
            "/api/users/change-initial-password", "/api/users/me/change-password");
    private final AuthenticationAttemptLimiter limiter;
    private final HandlerExceptionResolver exceptionResolver;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !PATHS.contains(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            limiter.checkClientIp(request.getRemoteAddr());
        } catch (AuthenticationRateLimitException | PersistenceException exception) {
            exceptionResolver.resolveException(request, response, null, exception);
            return;
        }
        chain.doFilter(request, response);
    }
}
