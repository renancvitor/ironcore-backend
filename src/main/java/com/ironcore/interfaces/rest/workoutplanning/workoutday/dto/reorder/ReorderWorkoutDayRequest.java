package com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.reorder;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReorderWorkoutDayRequest(
        @NotNull(message = "Dia da semana é obrigatório.")
        WeekDay weekDay,

        @NotNull(message = "A ordenação é obrigatória.")
        @Positive(message = "A ordenação deve ser positiva.")
        Integer sortOrder
) {
}
