package com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.create;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateWorkoutDayRequest(
        @NotNull(message = "O ciclo de treino é obrigatório.")
        @Positive(message = "O id do ciclo deve ser positivo.")
        Long workoutCycleId,

        @NotNull(message = "Dia da semana é obrigatório.")
        WeekDay weekDay,

        @NotBlank(message = "O título é obrigatório.")
        String title
) {
}
