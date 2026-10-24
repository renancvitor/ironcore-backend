package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.create;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateWorkoutCycleRequest(
        @NotBlank(message = "O nome é obrigatório.")
        String name,

        @NotNull(message = "O objetivo de treino é obrigatório.")
        @Positive(message = "O id do objetivo de treino deve ser positivo.")
        Long trainingGoalId,

        @Positive(message = "A duração desejada em meses deve ser positiva.")
        Integer desiredDurationMonths,

        @Schema(description = "Dia inicial da semana para apresentação no frontend. Omitido ou nulo assume SUNDAY.", example = "MONDAY", defaultValue = "SUNDAY", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        WeekDay weekStartsOn,
        String notes
) {
}
