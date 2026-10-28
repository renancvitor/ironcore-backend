package com.ironcore.interfaces.rest.workoutsession.dto.analytics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ExerciseCardioEvolutionPointResponse(
        Long sessionId,
        LocalDate performedOn,
        Instant startedAt,
        String exerciseNameSnapshot,
        @Schema(description = "Distância registrada em km; null sem registro.", nullable = true)
        BigDecimal distanceKm,
        @Schema(description = "Duração informada em minutos inteiros; null sem registro.", nullable = true)
        Long durationMinutes,
        @Schema(description = "Média da duração informada em min/km, duas casas HALF_UP; não comprova tempo em movimento.", nullable = true)
        BigDecimal averagePaceMinPerKm,
        @Schema(description = "Média da duração informada em km/h, duas casas HALF_UP; não é velocidade instantânea.", nullable = true)
        BigDecimal averageSpeedKmPerHour,
        long activityCount,
        long distanceRecordCount,
        long durationRecordCount,
        long paceRecordCount,
        long speedRecordCount,
        String workoutDayTitleSnapshot
) {
}
