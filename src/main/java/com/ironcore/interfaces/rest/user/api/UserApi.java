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
            description = "Permite alterar a senha do usuário após autenticação completa."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Senha alterada com sucesso."
    )
    @BadRequestResponse
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
            description = "Permite alterar a senha inicial do usuário antes da autenticação completa."
    )
    @SecurityRequirements
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
