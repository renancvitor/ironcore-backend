package com.ironcore.interfaces.rest.workoutsession.dto.detail;

import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

public record WorkoutSessionSetDetailResponse(
        Long id,
        Integer setNumber,
        BigDecimal actualLoadKg,
        ActualLoadMode actualLoadMode,
        Integer actualReps,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        @Schema(description = "Carga externa derivada: TOTAL preserva a carga; PER_SIDE multiplica por dois lados equivalentes. "
                + "Não presume peso de barra, corpo ou equipamento. Ausente quando não há carga.", nullable = true)
        BigDecimal normalizedLoadKg
) {
}
