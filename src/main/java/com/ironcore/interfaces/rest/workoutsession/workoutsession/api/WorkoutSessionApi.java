package com.ironcore.interfaces.rest.workoutsession.workoutsession.api;

import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.openapi.*;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.detail.GetWorkoutSessionDetailResponse;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.start.StartWorkoutSessionRequest;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.start.StartWorkoutSessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Sessão de treino")
public interface WorkoutSessionApi {

    @Operation(
            summary = "Iniciar sessão de treino",
            description = "Inicia uma sessão a partir de um dia do ciclo em andamento do usuário autenticado. "
                    + "Preserva a prescrição como snapshot, sem criar séries executadas ou preencher dados de execução. "
                    + "Somente uma sessão pode permanecer em andamento por pessoa."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Sessão de treino iniciada com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = StartWorkoutSessionResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @ConflictResponse
    @InternalServerErrorResponse
    ResponseEntity<StartWorkoutSessionResponse> start(
            AuthenticatedUser authenticatedUser,
            @Valid StartWorkoutSessionRequest request
    );

    @Operation(
            summary = "Consultar detalhe da sessão de treino",
            description = "Retorna os snapshots históricos e os dados de execução registrados na sessão do usuário "
                    + "autenticado, mesmo após alteração ou exclusão do planejamento. "
                    + "Referências à origem podem ser nulas; séries retornadas são somente as registradas."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Busca concluída.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GetWorkoutSessionDetailResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<GetWorkoutSessionDetailResponse> getDetail(AuthenticatedUser authenticatedUser, Long id);

    @Operation(
            summary = "Recuperar sessão de treino em andamento",
            description = "Retorna a sessão com status IN_PROGRESS da pessoa do usuário autenticado, incluindo "
                    + "snapshots e execução registrada para continuar o registro. "
                    + "Retorna 404 quando não existe sessão em andamento. A consulta não encerra ou expira a sessão."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Sessão em andamento encontrada.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GetWorkoutSessionDetailResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<GetWorkoutSessionDetailResponse> getInProgress(AuthenticatedUser authenticatedUser);
}
