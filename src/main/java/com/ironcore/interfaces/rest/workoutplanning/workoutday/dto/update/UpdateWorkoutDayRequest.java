package com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.update;

import jakarta.validation.constraints.NotBlank;

public record UpdateWorkoutDayRequest(
        @NotBlank(message = "O título é obrigatório.")
        String title
) {
}
