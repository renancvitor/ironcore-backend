package com.ironcore.interfaces.rest.workoutsession.api;

import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.workoutsession.dto.list.ListWorkoutSessionsResponse;
import com.ironcore.interfaces.rest.openapi.*;
import com.ironcore.interfaces.rest.workoutsession.dto.close.CloseWorkoutSessionResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.detail.GetWorkoutSessionDetailResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.detail.WorkoutSessionActivityDetailResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.detail.WorkoutSessionSetDetailResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.execution.UpdateWorkoutSessionActivityExecutionRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.loadmode.ChangeWorkoutSessionActivityLoadModeRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.notes.UpdateWorkoutSessionNotesRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.set.CreateWorkoutSessionSetRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.set.UpdateWorkoutSessionSetRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.start.StartWorkoutSessionRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.start.StartWorkoutSessionResponse;
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

@Tag(name = "Sessão de treino")
public interface WorkoutSessionApi {

    @Operation(summary = "Listar histórico de sessões encerradas",
            description = "Retorna somente sessões CLOSED da pessoa autenticada, encerradas manualmente ou por expiração. "
                    + "Consulta somente leitura, sem expirar sessões. Filtros opcionais combinados por AND: "
                    + "workoutCycleId e workoutDayId positivos; startDate e endDate em yyyy-MM-dd, "
                    + "inclusivos sobre performedOn. Uma única data limita somente seu lado do intervalo. "
                    + "Intervalo invertido retorna 422. Ordenação: performedOn DESC, startedAt DESC, id DESC. "
                    + "page inicia em 0; size padrão 20, entre 1 e 100. IDs sem correspondência retornam página vazia. "
                    + "Snapshots são preservados; referências removidas são nulas e não correspondem a filtros por ID de origem.")
    @ApiResponse(responseCode = "200", description = "Página de sessões encerradas.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ListWorkoutSessionsResponse.class)))
    @BadRequestResponse
    @UnprocessableEntityResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<ListWorkoutSessionsResponse> list(
            AuthenticatedUser authenticatedUser, Long workoutCycleId, Long workoutDayId,
            LocalDate startDate, LocalDate endDate, @Min(0) int page, @Min(1) @Max(100) int size);

    @Operation(summary = "Atualizar relato da sessão",
            description = "Atualiza ou limpa o relato geral. Omitir notes é inválido. Correções em sessões encerradas são permitidas sem reabertura.")
    @ApiResponse(responseCode = "200", description = "Operação concluída.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GetWorkoutSessionDetailResponse.class)))
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @ConflictResponse
    @InternalServerErrorResponse
    ResponseEntity<GetWorkoutSessionDetailResponse> updateNotes(AuthenticatedUser authenticatedUser, Long id, @Valid UpdateWorkoutSessionNotesRequest request);

    @Operation(summary = "Atualizar execução da atividade",
            description = "Atualiza somente campos presentes; null solicita limpeza. Duração e distância são independentes. Correções em sessões encerradas são permitidas sem reabertura.")
    @ApiResponse(responseCode = "200", description = "Operação concluída.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = WorkoutSessionActivityDetailResponse.class)))
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @ConflictResponse
    @InternalServerErrorResponse
    ResponseEntity<WorkoutSessionActivityDetailResponse> updateActivityExecution(AuthenticatedUser authenticatedUser, Long id, Long activityId, @Valid UpdateWorkoutSessionActivityExecutionRequest request);

    @Operation(summary = "Criar série executada",
            description = "Cria somente a série informada, sem copiar prescrições. Carga e repetições são independentes. Correções em sessões encerradas são permitidas sem reabertura.")
    @ApiResponse(responseCode = "201", description = "Operação concluída.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = WorkoutSessionSetDetailResponse.class)))
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @ConflictResponse
    @InternalServerErrorResponse
    ResponseEntity<WorkoutSessionSetDetailResponse> createSet(AuthenticatedUser authenticatedUser, Long id, Long activityId, @Valid CreateWorkoutSessionSetRequest request);

    @Operation(summary = "Atualizar série executada",
            description = "Atualiza somente campos presentes; null solicita limpeza. Uma série vazia deve ser excluída. Correções em sessões encerradas são permitidas sem reabertura.")
    @ApiResponse(responseCode = "200", description = "Operação concluída.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = WorkoutSessionSetDetailResponse.class)))
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @ConflictResponse
    @InternalServerErrorResponse
    ResponseEntity<WorkoutSessionSetDetailResponse> updateSet(AuthenticatedUser authenticatedUser, Long id, Long activityId, Long setId, @Valid UpdateWorkoutSessionSetRequest request);

    @Operation(summary = "Excluir série executada",
            description = "Exclui a série sem renumerar as demais. Correções em sessões encerradas são permitidas sem reabertura.")
    @ApiResponse(responseCode = "204", description = "Operação concluída.")
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @ConflictResponse
    @InternalServerErrorResponse
    ResponseEntity<Void> deleteSet(AuthenticatedUser authenticatedUser, Long id, Long activityId, Long setId);

    @Operation(summary = "Alterar modo de carga da atividade",
            description = "Reinterpreta as cargas de todos os sets carregados sem alterar os valores digitados. PER_SIDE considera dois lados equivalentes explicitamente informados; não presume peso de barra ou corpo. Correções em sessões encerradas são permitidas sem reabertura.")
    @ApiResponse(responseCode = "200", description = "Operação concluída.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = WorkoutSessionActivityDetailResponse.class)))
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @ConflictResponse
    @InternalServerErrorResponse
    ResponseEntity<WorkoutSessionActivityDetailResponse> changeActivityLoadMode(AuthenticatedUser authenticatedUser, Long id, Long activityId, @Valid ChangeWorkoutSessionActivityLoadModeRequest request);

    @Operation(
            summary = "Encerrar sessão de treino",
            description = "Encerra a sessão do usuário autenticado preservando snapshots e execução registrada. "
                    + "No horário limite ou depois, o domínio registra AUTO_EXPIRED com o horário limite como "
                    + "finishedAt e o instante do processamento como updatedAt. Sessões já encerradas são rejeitadas."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Sessão de treino encerrada com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CloseWorkoutSessionResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @ConflictResponse
    @InternalServerErrorResponse
    ResponseEntity<CloseWorkoutSessionResponse> close(AuthenticatedUser authenticatedUser, Long id);

    @Operation(
            summary = "Iniciar sessão de treino",
            description = "Inicia uma sessão a partir de um dia do ciclo em andamento do usuário autenticado. "
                    + "Preserva a prescrição como snapshot, sem criar séries executadas ou preencher dados de execução. "
                    + "Somente uma sessão pode permanecer em andamento por pessoa. "
                    + "Uma sessão anterior vencida é expirada antes de persistir o novo início."
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
