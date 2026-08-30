package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.api;

import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.openapi.*;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.cancel.CancelWorkoutCycleResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.complete.CompleteWorkoutCycleResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.create.CreateWorkoutCycleRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.create.CreateWorkoutCycleResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.detail.GetWorkoutCycleDetailResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.list.ListWorkoutCyclesResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.start.StartWorkoutCycleResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.update.UpdateWorkoutCycleRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.update.UpdateWorkoutCycleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

@Tag(name = "Ciclo de treino")
public interface WorkoutCycleApi {

    @Operation(
            summary = "Cadastro de ciclo de treino",
            description = "Permite ao usuário cadastrar um ciclo de treino."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Cadastro realizado com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateWorkoutCycleResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<CreateWorkoutCycleResponse> create(
            AuthenticatedUser authenticatedUser,
            @Valid CreateWorkoutCycleRequest request
    );

    @Operation(
            summary = "Alterar ciclo de treino",
            description = "Atualiza o ciclo de treino nos status 'Não iniciado' ou 'Em progresso'."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ciclo de treino alterado com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UpdateWorkoutCycleResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<UpdateWorkoutCycleResponse> update(
            AuthenticatedUser authenticatedUser,
            Long id,
            @Valid UpdateWorkoutCycleRequest update
    );

    @Operation(
            summary = "Deletar ciclo de treino",
            description = "Exclui o ciclo de treino quando estiver em status 'Não iniciado'."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Dados deletados com sucesso."
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @UnprocessableEntityResponse
    @InternalServerErrorResponse
    ResponseEntity<Void> delete(AuthenticatedUser authenticatedUser, Long id);

    @Operation(
            summary = "Iniciar ciclo de treino",
            description = "Inicia um ciclo de treino em status 'Não iniciado', desde que possua composição válida."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ciclo de treino iniciado com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = StartWorkoutCycleResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<StartWorkoutCycleResponse> start(AuthenticatedUser authenticatedUser, Long id);

    @Operation(
            summary = "Concluir ciclo de treino",
            description = "Conclui um ciclo de treino em status 'Em progresso'."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ciclo de treino concluído com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CompleteWorkoutCycleResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<CompleteWorkoutCycleResponse> complete(AuthenticatedUser authenticatedUser, Long id);

    @Operation(
            summary = "Cancelar ciclo de treino",
            description = "Cancela um ciclo de treino que ainda não esteja concluído ou cancelado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ciclo de treino cancelado com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CancelWorkoutCycleResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<CancelWorkoutCycleResponse> cancel(AuthenticatedUser authenticatedUser, Long id);

    @Operation(
            summary = "Busca ciclo de treino",
            description = "Busca ciclo de treino por ID do ciclo de treino do usuário autenticado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Busca concluída.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GetWorkoutCycleDetailResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<GetWorkoutCycleDetailResponse> getDetail(AuthenticatedUser authenticatedUser, Long id);

    @Operation(
            summary = "Listagem de ciclos de treino",
            description = "Lista os ciclos de treino do usuário autenticado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listagem concluída.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ListWorkoutCyclesResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<ListWorkoutCyclesResponse> list(
            AuthenticatedUser authenticatedUser,
            WorkoutStatus workoutStatus,
            Long trainingGoalId,
            LocalDate startDate,
            LocalDate endDate,
            String name,
            @Min(0) int page,
            @Min(1) @Max(100) int size
    );
}
