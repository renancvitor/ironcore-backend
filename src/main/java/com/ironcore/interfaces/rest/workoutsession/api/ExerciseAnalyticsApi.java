package com.ironcore.interfaces.rest.workoutsession.api;

import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.openapi.BadRequestResponse;
import com.ironcore.interfaces.rest.openapi.ForbiddenResponse;
import com.ironcore.interfaces.rest.openapi.InternalServerErrorResponse;
import com.ironcore.interfaces.rest.openapi.NotFoundResponse;
import com.ironcore.interfaces.rest.openapi.UnauthorizedResponse;
import com.ironcore.interfaces.rest.openapi.UnprocessableEntityResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.analytics.ExerciseCardioEvolutionResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.analytics.ExerciseStrengthEvolutionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

@Tag(name = "Indicadores de execução")
public interface ExerciseAnalyticsApi {

    String PERIOD_DESCRIPTION = "Somente CLOSED (MANUAL e AUTO_EXPIRED) da pessoa autenticada, sem expirar sessões. "
            + "exerciseId positivo, sem exigir catálogo ativo; ID sem registros retorna points vazio. "
            + "startDate e endDate obrigatórios, inclusivos sobre performedOn, sem datas futuras no fuso America/Sao_Paulo. "
            + "Máximo 12 meses: endDate <= startDate + 12 meses - 1 dia. Período inválido retorna 422; parâmetro ausente ou malformado retorna 400. "
            + "Sem paginação ou consolidação mensal. Um ponto por exercício e sessão, mesmo sem valores numéricos. "
            + "Ordem performedOn ASC, startedAt ASC, sessionId ASC. Nome histórico da atividade de menor ordem. "
            + "Execução corrigida reflete na consulta; sem fallback planejado ou reconstrução de referências excluídas. ";

    @Operation(operationId = "strength", summary = "Consultar evolução de carga e volume", description = PERIOD_DESCRIPTION
            + "Carga TOTAL preservada; PER_SIDE × 2. Maior carga normalizada exige carga, modo válido e repetições >= 1. "
            + "Séries com carga sem repetições são preservadas na cobertura, mas não participam do máximo ou volume. "
            + "Volume em kg·rep soma apenas séries com carga e repetições. Zero informado é preservado; ausência retorna null. "
            + "Contagens representam cobertura dos registros, não cumprimento da prescrição.")
    @ApiResponse(responseCode = "200", description = "Todos os pontos do período.",
            content = @Content(schema = @Schema(implementation = ExerciseStrengthEvolutionResponse.class)))
    @BadRequestResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @ForbiddenResponse
    @NotFoundResponse
    @InternalServerErrorResponse
    ResponseEntity<ExerciseStrengthEvolutionResponse> getStrengthEvolution(
            @Parameter(hidden = true) AuthenticatedUser authenticatedUser,
            Long exerciseId,
            @Parameter(required = true, description = "Data inicial inclusiva, yyyy-MM-dd.") LocalDate startDate,
            @Parameter(required = true, description = "Data final inclusiva, yyyy-MM-dd.") LocalDate endDate);

    @Operation(operationId = "cardio", summary = "Consultar evolução de distância, duração e médias", description = PERIOD_DESCRIPTION
            + "Distância km e duração min são independentes. Pace min/km exige duração e distância > 0; "
            + "velocidade km/h exige duração > 0 e distância informada, inclusive zero. "
            + "Elegibilidade por atividade antes da agregação, sem combinar valores isolados de atividades diferentes. "
            + "Resumo do período completo usa razões dos totais elegíveis, nunca média aritmética dos indicadores. "
            + "Derivados com duas casas HALF_UP, sem arredondamento intermediário. Minutos de origem são inteiros. "
            + "Pace e velocidade são médias da duração informada, sem afirmar tempo em movimento, modalidade ou velocidade instantânea. "
            + "Campos ausentes permanecem null; totais sem medições são null.")
    @ApiResponse(responseCode = "200", description = "Todos os pontos e resumo do período.",
            content = @Content(schema = @Schema(implementation = ExerciseCardioEvolutionResponse.class)))
    @BadRequestResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @ForbiddenResponse
    @NotFoundResponse
    @InternalServerErrorResponse
    ResponseEntity<ExerciseCardioEvolutionResponse> getCardioEvolution(
            @Parameter(hidden = true) AuthenticatedUser authenticatedUser,
            Long exerciseId,
            @Parameter(required = true, description = "Data inicial inclusiva, yyyy-MM-dd.") LocalDate startDate,
            @Parameter(required = true, description = "Data final inclusiva, yyyy-MM-dd.") LocalDate endDate);
}
