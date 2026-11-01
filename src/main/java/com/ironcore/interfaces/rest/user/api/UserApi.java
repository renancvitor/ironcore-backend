package com.ironcore.interfaces.rest.user.api;

import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.openapi.*;
import com.ironcore.interfaces.rest.user.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Usuário")
public interface UserApi {

    @Operation(
            summary = "Alterar senha",
            description = "Requer autenticação e CSRF. Em uma transação altera a senha, revoga todas as sessões anteriores e cria nova sessão com cookies access_token e refresh_token no navegador atual, preservando sua autenticação. Os demais dispositivos precisam realizar login. Credenciais anteriores deixam de funcionar; não há tokens no JSON."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Senha alterada com sucesso."
    )
    @BadRequestResponse
    @ApiResponse(responseCode = "429", description = "Limite de 10 tentativas por usuário em 5 minutos. Retry-After: 300.")
    @UnauthorizedResponse
    @UnprocessableEntityResponse
    @ForbiddenResponse
    @NotFoundResponse
    @InternalServerErrorResponse
    ResponseEntity<Void> changePassword(
            AuthenticatedUser authenticatedUser,
            ChangePasswordRequest request
    );

    @Operation(
            summary = "Alterar senha inicial",
            description = "Requer CSRF e credenciais da senha inicial. Revoga todas as sessões antigas sem criar sessão nem emitir credenciais. Após a troca é necessário realizar login."
    )
    @SecurityRequirements
    @ApiResponse(responseCode = "429", description = "Limite de 10 tentativas por conta em 5 minutos compartilhado com login. Retry-After: 300.")
    @ApiResponse(
            responseCode = "204",
            description = "Senha alterada com sucesso."
    )
    @BadRequestResponse
    @UnauthorizedResponse
    @UnprocessableEntityResponse
    @ForbiddenResponse
    @InternalServerErrorResponse
    ResponseEntity<Void>  changeInitialPassword(InitialChangePasswordRequest request);

    @Operation(
            summary = "Obter usuário autenticado",
            description = "Busca e retorna usuário autenticado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Usuário autenticado retornado com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserResponse.class)
            )
    )
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<UserResponse> getAuthenticatedUser(AuthenticatedUser authenticatedUser);

    @Operation(
            summary = "Alterar apelido",
            description = "Permite alterar o apelido do usuário."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Apelido alterado com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ChangeNicknameResponse.class)
            )
    )
    @BadRequestResponse
    @UnauthorizedResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnprocessableEntityResponse
    @InternalServerErrorResponse
    ResponseEntity<ChangeNicknameResponse> changeNickname(
            AuthenticatedUser authenticatedUser,
            ChangeNicknameRequest request
    );
}
