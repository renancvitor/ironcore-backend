package com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.start;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StartWorkoutSessionRequest(
        @NotNull(message = "O ciclo de treino é obrigatório.")
        @Positive(message = "O id do ciclo de treino deve ser positivo.")
        Long workoutCycleId,

        @NotNull(message = "O dia de treino é obrigatório.")
        @Positive(message = "O id do dia de treino deve ser positivo.")
        Long workoutDayId
) {
}
