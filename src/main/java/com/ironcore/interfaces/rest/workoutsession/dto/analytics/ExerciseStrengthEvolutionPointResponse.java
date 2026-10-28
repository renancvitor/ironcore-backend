package com.ironcore.interfaces.rest.workoutsession.dto.analytics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ExerciseStrengthEvolutionPointResponse(
        Long sessionId,
        LocalDate performedOn,
        Instant startedAt,
        String exerciseNameSnapshot,
        @Schema(description = "Maior carga normalizada em kg entre séries com carga, modo válido e repetições >= 1. Null sem séries elegíveis; não representa 1RM.", nullable = true)
        BigDecimal maxNormalizedLoadKg,
        @Schema(description = "Soma de carga normalizada × repetições informadas, em kg·rep. Null sem séries calculáveis.", nullable = true)
        BigDecimal volumeKgReps,
        long registeredSetCount,
        long loadedSetCount,
        long volumeSetCount
) {
}
