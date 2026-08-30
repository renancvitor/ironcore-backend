package com.ironcore.interfaces.rest.workoutplanning.workoutday.api;

import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.openapi.*;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.create.CreateWorkoutDayRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.create.CreateWorkoutDayResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.reorder.ReorderWorkoutDayRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.update.UpdateWorkoutDayRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.update.UpdateWorkoutDayResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Dia de treino")
public interface WorkoutDayApi {

    @Operation(
            summary = "Cadastro de dia de treino",
            description = "Permite ao usuário cadastrar um dia de treino."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Cadastro realizado com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateWorkoutDayResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<CreateWorkoutDayResponse> create(
            AuthenticatedUser authenticatedUser,
            @Valid CreateWorkoutDayRequest request
    );

    @Operation(
            summary = "Alterar dia de treino",
            description = "Atualiza parcialmente o dia de treino vinculado ao usuário autenticado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Dia de treino alterado com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UpdateWorkoutDayResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<UpdateWorkoutDayResponse> update(
            AuthenticatedUser authenticatedUser,
            Long id,
            @Valid UpdateWorkoutDayRequest request
    );

    @Operation(
            summary = "Deletar dia de treino",
            description = "Deleta o registro de dia de treino do usuário autenticado."
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
            summary = "Reordenar dia de treino",
            description = "Reordena o dia de treino vinculado ao usuário autenticado."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Dia de treino reordenado com sucesso."
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
            @Valid ReorderWorkoutDayRequest request
    );
}
