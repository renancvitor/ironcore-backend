package com.ironcore.interfaces.rest.auth;

import com.ironcore.application.auth.port.SessionCredentials;
import com.ironcore.application.auth.usecase.*;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.infrastructure.security.cookie.AccessTokenCookieFactory;
import com.ironcore.infrastructure.security.cookie.RefreshTokenCookieFactory;
import com.ironcore.interfaces.rest.auth.api.AuthApi;
import com.ironcore.interfaces.rest.auth.dto.LoginRequest;
import com.ironcore.interfaces.rest.auth.dto.LoginResponse;
import com.ironcore.interfaces.rest.auth.dto.RefreshResponse;
import com.ironcore.interfaces.rest.auth.mapper.AuthRestMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final CsrfTokenRepository csrfTokenRepository;
    private final AccessTokenCookieFactory accessTokenCookieFactory;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;
    private final RefreshSessionUseCase refreshSessionUseCase;

    @Override
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        LoginCommand command = AuthRestMapper.toCommand(request);
        LoginResult result = loginUseCase.execute(command);
        LoginResponse response = AuthRestMapper.toResponse(result);

        csrfTokenRepository.saveToken(null, servletRequest, servletResponse);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookieFactory.create(result.credentials().accessToken()),
                        refreshTokenCookieFactory.create(result.credentials()))
                .body(response);
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    ) {
        SessionCredentials credentials = refreshSessionUseCase.execute(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookieFactory.create(credentials.accessToken()),
                        refreshTokenCookieFactory.create(credentials))
                .body(new RefreshResponse(credentials.accessToken().expiresAt()));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        logoutUseCase.execute(new LogoutCommand(
                authenticatedUser == null ? null : authenticatedUser.userId(),
                authenticatedUser == null ? null : authenticatedUser.sessionId(),
                refreshToken
        ));

        csrfTokenRepository.saveToken(null, servletRequest, servletResponse);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookieFactory.clear(), refreshTokenCookieFactory.clear())
                .build();
    }
}
