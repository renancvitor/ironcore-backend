package com.ironcore.interfaces.rest.auth.api;

import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.auth.dto.LoginRequest;
import com.ironcore.interfaces.rest.auth.dto.LoginResponse;
import com.ironcore.interfaces.rest.openapi.BadRequestResponse;
import com.ironcore.interfaces.rest.openapi.ForbiddenResponse;
import com.ironcore.interfaces.rest.openapi.InternalServerErrorResponse;
import com.ironcore.interfaces.rest.openapi.UnauthorizedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Autenticação")
public interface AuthApi {

    @Operation(
            summary = "Autenticar usuário",
            description = "Requer CSRF. Cada login cria sessão independente por contexto de cookies, compartilhada entre abas. Emite access_token (Path=/) e refresh_token (Path=/api/auth), HttpOnly/Secure, host-only, SameSite=None local e Lax em produção. Refresh expira em no máximo 7 dias desde o login (padrão 7), sem extensão. Credenciais nunca são retornadas em JSON. Reobtenha CSRF após login."
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
    @ApiResponse(responseCode = "429", description = "Limite compartilhado de login/troca inicial: 10 tentativas por conta em 5 minutos, independentemente da existência da conta. Retry-After: 300.")
    @UnauthorizedResponse
    @ForbiddenResponse
    @InternalServerErrorResponse
    ResponseEntity<LoginResponse> login(LoginRequest request, HttpServletRequest servletRequest,
                                        HttpServletResponse servletResponse);

    @Operation(summary = "Renovar credenciais", description = "Requer CSRF e cookie refresh_token. Rotaciona a credencial uma única vez, respeitando o limite absoluto de 7 dias. Retorna novos cookies HttpOnly/Secure, sem tokens no JSON. Não limpa CSRF. Access token configurável: 120 minutos durante integração; 15 após frontend #106.")
    @SecurityRequirements
    @ApiResponse(responseCode = "204", description = "Novos cookies access_token e refresh_token; a credencial anterior foi invalidada.")
    @ApiResponse(responseCode = "409", description = "Credencial anterior apresentada até 10 segundos após rotação: sem Set-Cookie e sem revogação. Aguarde os cookies compartilhados da requisição concorrente e tente novamente uma vez. Nunca emite tokens para credencial consumida. Fora dessa janela retorna 401, sem revogar a sessão. Resposta perdida pode exigir novo login.")
    @UnauthorizedResponse
    @ForbiddenResponse
    @InternalServerErrorResponse
    ResponseEntity<Void> refresh(HttpServletRequest request);

    @Operation(
            summary = "Encerrar sessão",
            description = "Requer CSRF. Revoga apenas a sessão atual identificada por access token válido ou refresh token atual, inclusive com access token expirado/ausente. Sem credenciais válidas apenas limpa cookies, sem inferir identidade. Idempotente; expira ambos os cookies com atributos compatíveis e limpa CSRF. Não encerra outros dispositivos."
    )
    @SecurityRequirements
    @ApiResponse(
            responseCode = "204",
            description = "Revoga a sessão identificável e expira os cookies de acesso e refresh."
    )
    @InternalServerErrorResponse
    @ForbiddenResponse
    ResponseEntity<Void> logout(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    );
}
