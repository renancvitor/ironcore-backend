package com.ironcore.interfaces.rest.auth;

import com.ironcore.application.auth.usecase.LoginCommand;
import com.ironcore.application.auth.usecase.LoginResult;
import com.ironcore.application.auth.usecase.LoginUseCase;
import com.ironcore.application.auth.usecase.LogoutUseCase;
import com.ironcore.interfaces.rest.auth.dto.LoginRequest;
import com.ironcore.interfaces.rest.auth.dto.LoginResponse;
import com.ironcore.interfaces.rest.auth.mapper.AuthRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

@Tag(name = "Autenticação")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final Clock clock;

    @Operation(
            summary = "Autenticar usuário",
            description = "Autentica o usuário e cria o cookie HTTP-only de acesso."
    )
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = AuthRestMapper.toCommand(request);
        LoginResult result = loginUseCase.execute(command);
        LoginResponse response = AuthRestMapper.toResponse(result);
        Duration cookieMaxAge = Duration.between(LocalDateTime.now(clock), result.expiresAt());

        ResponseCookie cookie = ResponseCookie.from("access_token", result.accessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(cookieMaxAge.isNegative() ? Duration.ZERO : cookieMaxAge)
                .sameSite("None")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(response);
    }

    @Operation(
            summary = "Encerrar sessão",
            description = "Remove cookie de autenticação do usuário."
    )
    @SecurityRequirements
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        logoutUseCase.execute();

        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }
}
