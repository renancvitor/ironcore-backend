package com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.WorkoutSessionIntegerDeserializer;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.WorkoutSessionLoadModeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateWorkoutSessionSetRequest(
        @NotNull @Positive
        @JsonDeserialize(using = WorkoutSessionIntegerDeserializer.class)
        Integer setNumber,
        BigDecimal actualLoadKg,
        @Schema(description = "Obrigatório com carga. PER_SIDE considera dois lados equivalentes explicitamente informados.")
        @JsonDeserialize(using = WorkoutSessionLoadModeDeserializer.class)
        ActualLoadMode actualLoadMode,
        @JsonDeserialize(using = WorkoutSessionIntegerDeserializer.class)
        Integer actualReps,
        String notes
) {
}
