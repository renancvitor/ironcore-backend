package com.ironcore.interfaces.rest.workoutsession.dto.analytics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ExerciseCardioEvolutionSummaryResponse(
        @Schema(description = "Soma das distâncias informadas no período, em km.", nullable = true)
        BigDecimal totalDistanceKm,
        @Schema(description = "Soma das durações informadas no período, em minutos inteiros.", nullable = true)
        Long totalDurationMinutes,
        @Schema(description = "Min/km pela razão dos totais elegíveis por atividade, não média aritmética. Média da duração informada.", nullable = true)
        BigDecimal averagePaceMinPerKm,
        @Schema(description = "Km/h pela razão dos totais elegíveis por atividade, não média aritmética. Média da duração informada.", nullable = true)
        BigDecimal averageSpeedKmPerHour,
        long distanceRecordCount,
        long durationRecordCount,
        long paceRecordCount,
        long speedRecordCount
) {
}
