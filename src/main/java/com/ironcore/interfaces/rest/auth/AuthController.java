package com.ironcore.interfaces.rest.auth;

import com.ironcore.application.auth.port.GeneratedAccessToken;
import com.ironcore.application.auth.usecase.*;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.infrastructure.security.cookie.AccessTokenCookieFactory;
import com.ironcore.interfaces.rest.auth.api.AuthApi;
import com.ironcore.interfaces.rest.auth.dto.LoginRequest;
import com.ironcore.interfaces.rest.auth.dto.LoginResponse;
import com.ironcore.interfaces.rest.auth.mapper.AuthRestMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LoginUseCase loginUseCase;
    private final com.ironcore.application.auth.port.AuthenticationAttemptGuard attemptGuard;
    private final LogoutUseCase logoutUseCase;
    private final CsrfTokenRepository csrfTokenRepository;
    private final AccessTokenCookieFactory accessTokenCookieFactory;

    @Override
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        LoginCommand command = AuthRestMapper.toCommand(request);
        attemptGuard.check(command.email().value());
        LoginResult result = loginUseCase.execute(command);
        LoginResponse response = AuthRestMapper.toResponse(result);

        csrfTokenRepository.saveToken(null, servletRequest, servletResponse);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookieFactory.create(
                        new GeneratedAccessToken(
                                result.accessToken(),
                                result.tokenType(),
                                result.expiresAt()
                        )
                ))
                .header(HttpHeaders.SET_COOKIE, accessTokenCookieFactory.createRefresh(result.refreshToken(), result.refreshExpiresAt()))
                .body(response);
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request) {
        GeneratedAccessToken token = refreshTokenUseCase.execute(refreshCookie(request));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookieFactory.create(token),
                        accessTokenCookieFactory.createRefresh(token.refreshToken(), token.refreshExpiresAt()))
                .build();
    }

    private String refreshCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        if (authenticatedUser != null) {
            logoutUseCase.execute(new LogoutCommand(
                    authenticatedUser.userId(),
                    authenticatedUser.sessionId()
            ));
        }

        if (authenticatedUser == null) {
            logoutUseCase.executeWithRefreshToken(refreshCookie(servletRequest));
        }
        csrfTokenRepository.saveToken(null, servletRequest, servletResponse);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookieFactory.clear(), accessTokenCookieFactory.clearRefresh())
                .build();
    }
}
