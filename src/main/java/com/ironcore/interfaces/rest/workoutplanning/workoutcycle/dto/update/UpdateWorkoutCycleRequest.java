package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.update;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateWorkoutCycleRequest(
        @NotBlank(message = "O nome é obrigatório.")
        String name,

        @NotNull(message = "O objetivo de treino é obrigatório.")
        @Positive(message = "O id do objetivo de treino deve ser positivo.")
        Long trainingGoalId,

        @Positive(message = "A duração desejada em meses deve ser positiva.")
        Integer desiredDurationMonths,

        @Schema(description = "Dia inicial da semana para apresentação no frontend. Omitido ou nulo preserva o valor atual. Editável em NOT_STARTED e IN_PROGRESS.", example = "MONDAY", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        WeekDay weekStartsOn,
        String notes
) {
}
