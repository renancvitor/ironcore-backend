package com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.reorder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReorderWorkoutActivityRequest(
        @NotNull(message = "A ordenação é obrigatória.")
        @Positive(message = "A ordenação deve ser positiva.")
        Integer orderIndex
) {
}
