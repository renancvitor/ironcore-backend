package com.ironcore.interfaces.rest.workoutplanning.workoutactivity.api;

import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.openapi.*;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.create.CreateWorkoutActivityRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.create.CreateWorkoutActivityResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.reorder.ReorderWorkoutActivityRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.update.UpdateWorkoutActivityRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.update.UpdateWorkoutActivityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Atividade de treino")
public interface WorkoutActivityApi {

    @Operation(
            summary = "Cadastro de atividade de treino",
            description = "Permite ao usuário cadastrar uma atividade de treino."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Cadastro realizado com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateWorkoutActivityResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<CreateWorkoutActivityResponse> create(
            AuthenticatedUser authenticatedUser,
            @Valid CreateWorkoutActivityRequest request
    );

    @Operation(
            summary = "Alterar atividade de treino",
            description = "Atualiza parcialmente a atividade de treino vinculada ao usuário autenticado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Atividade de treino alterada com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UpdateWorkoutActivityResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<UpdateWorkoutActivityResponse> update(
            AuthenticatedUser authenticatedUser,
            Long id,
            @Valid UpdateWorkoutActivityRequest request
    );

    @Operation(
            summary = "Deletar atividade de treino",
            description = "Deleta o registro de atividade de treino do usuário autenticado."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Dados deletados com sucesso."
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<Void> delete(AuthenticatedUser authenticatedUser, Long id);

    @Operation(
            summary = "Reordenar atividade de treino",
            description = "Reordena a atividade de treino vinculada ao usuário autenticado."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Atividade de treino reordenada com sucesso."
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<Void> reorder(
            AuthenticatedUser authenticatedUser,
            Long id,
            @Valid ReorderWorkoutActivityRequest request
    );
}
