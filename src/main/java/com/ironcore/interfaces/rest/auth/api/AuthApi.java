package com.ironcore.interfaces.rest.auth.api;

import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.auth.dto.LoginRequest;
import com.ironcore.interfaces.rest.auth.dto.LoginResponse;
import com.ironcore.interfaces.rest.auth.dto.RefreshResponse;
import com.ironcore.interfaces.rest.openapi.BadRequestResponse;
import com.ironcore.interfaces.rest.openapi.ForbiddenResponse;
import com.ironcore.interfaces.rest.openapi.InternalServerErrorResponse;
import com.ironcore.interfaces.rest.openapi.TooManyRequestsResponse;
import com.ironcore.interfaces.rest.openapi.UnauthorizedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Autenticação")
public interface AuthApi {

    @Operation(
            summary = "Autenticar usuário",
            description = "Autentica o usuário e envia o JWT no cookie HTTP-only `access_token`."
    )
    @SecurityRequirements
    @ApiResponse(
            responseCode = "200",
            description = "Usuário autenticado com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LoginResponse.class)
            )
    )
    @BadRequestResponse
    @UnauthorizedResponse
    @ForbiddenResponse
    @InternalServerErrorResponse
    @TooManyRequestsResponse
    ResponseEntity<LoginResponse> login(LoginRequest request, HttpServletRequest servletRequest,
                                        HttpServletResponse servletResponse);

    @Operation(
            summary = "Renovar sessão",
            description = "Rotaciona o refresh token e renova o acesso por cookies HttpOnly. Exige CSRF. "
                    + "O refresh anterior deixa de ser aceito; renovações concorrentes devem ser serializadas pelo cliente."
    )
    @SecurityRequirements
    @ApiResponse(responseCode = "200", description = "Cookies renovados sem estender a validade absoluta da sessão.")
    @UnauthorizedResponse
    @ForbiddenResponse
    @InternalServerErrorResponse
    @TooManyRequestsResponse
    ResponseEntity<RefreshResponse> refresh(String refreshToken);

    @Operation(
            summary = "Encerrar sessão",
            description = "Revoga a sessão identificada pelo acesso ou refresh token e expira ambos os cookies."
    )
    @SecurityRequirements
    @ApiResponse(
            responseCode = "204",
            description = "Revoga a sessão, quando identificável, e expira os cookies de acesso e refresh."
    )
    @InternalServerErrorResponse
    ResponseEntity<Void> logout(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            String refreshToken,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    );
}
