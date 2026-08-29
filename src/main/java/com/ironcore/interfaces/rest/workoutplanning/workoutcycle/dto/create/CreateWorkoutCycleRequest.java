package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.create;

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

        String notes
) {
}
