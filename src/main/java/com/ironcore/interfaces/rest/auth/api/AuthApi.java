package com.ironcore.interfaces.rest.auth.api;

import com.ironcore.interfaces.rest.auth.dto.LoginRequest;
import com.ironcore.interfaces.rest.auth.dto.LoginResponse;
import com.ironcore.interfaces.rest.openapi.BadRequestResponse;
import com.ironcore.interfaces.rest.openapi.ForbiddenResponse;
import com.ironcore.interfaces.rest.openapi.InternalServerErrorResponse;
import com.ironcore.interfaces.rest.openapi.UnauthorizedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Autenticação")
public interface AuthApi {

    @Operation(
            summary = "Autenticar usuário",
            description = "Autentica o usuário e envia o JWT no cookie HTTP-only `access_token`."
    )
    @SecurityRequirements
    @ApiResponse(
            responseCode = "200",
            description = "Usuário autenticado com sucesso",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE
            )
    )
    @BadRequestResponse
    @UnauthorizedResponse
    @ForbiddenResponse
    @InternalServerErrorResponse
    ResponseEntity<LoginResponse> login(LoginRequest request);

    @Operation(
            summary = "Encerrar sessão",
            description = "Expira o cookie HTTP-only `access_token`, encerrando a sessão."
    )
    @SecurityRequirements
    @ApiResponse(
            responseCode = "204",
            description = "Sessão encerrada com sucesso"
    )
    ResponseEntity<Void> logout();
}
