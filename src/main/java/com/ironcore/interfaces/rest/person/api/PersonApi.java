package com.ironcore.interfaces.rest.person.api;

import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.openapi.*;
import com.ironcore.interfaces.rest.person.dto.UpdatePersonRequest;
import com.ironcore.interfaces.rest.person.dto.UpdatePersonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Pessoa")
public interface PersonApi {

    @Operation(
            summary = "Alterar nome, sexo ou data de nascimento",
            description = "Atualiza parcialmente os dados pessoais vinculados ao usuário autenticado. " +
                    "Envie ao menos um dos campos: nome, sexo ou data de nascimento."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Dados pessoais alterados com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UpdatePersonResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<UpdatePersonResponse> update(
            AuthenticatedUser authenticatedUser,
            UpdatePersonRequest request
    );
}
