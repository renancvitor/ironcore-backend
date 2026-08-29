package com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.update;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateWorkoutActivityRequest(
        @NotNull(message = "O exercício é obrigatório.")
        @Positive(message = "O id do exercício deve ser positivo.")
        Long exerciseId,

        @Positive(message = "A quantidade de séries deve ser positiva.")
        Integer sets,

        @Positive(message = "A quantidade de repetições mínimas deve ser positiva.")
        Integer repRangeMin,

        @Positive(message = "A quantidade de repetições máximas deve ser positiva.")
        Integer repRangeMax,

        @Positive(message = "A carga alvo deve ser positiva.")
        BigDecimal targetLoadKg,

        @Pattern(
                regexp = ".*\\S.*",
                message = "O alvo de carga não pode ser vazio."
        )
        String targetLoadText,

        @Positive(message = "A duração em minutos deve ser positiva.")
        Integer durationMinutes,

        @Positive(message = "A distância em quilômetros deve ser positiva.")
        BigDecimal distanceKm,

        @Pattern(
                regexp = ".*\\S.*",
                message = "A intensidade não pode ser vazia."
        )
        String intensityText,

        @Positive(message = "O descanso em segundos deve ser positivo.")
        Integer restSeconds,

        @Pattern(
                regexp = ".*\\S.*",
                message = "As observações não podem ser vazias."
        )
        String notes
) {
}
