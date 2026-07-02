package com.ironcore.interfaces.rest.bodymetrics.dto;

import jakarta.validation.constraints.Positive;

public record BodyCircumferencesRequest(
        @Positive(message = "Circunferência do pescoço deve ser maior do que zero.")
        Double neckCm,

        @Positive(message = "Circunferência do peitoral deve ser maior do que zero.")
        Double chestCm,

        @Positive(message = "Circunferência do ombro deve ser maior do que zero.")
        Double shoulderCm,

        @Positive(message = "Circunferência do braço deve ser maior do que zero.")
        Double armCm,

        @Positive(message = "Circunferência do antebraço deve ser maior do que zero.")
        Double forearmCm,

        @Positive(message = "Circunferência da cintura deve ser maior do que zero.")
        Double waistCm,

        @Positive(message = "Circunferência do quadril deve ser maior do que zero.")
        Double hipCm,

        @Positive(message = "Circunferência da coxa deve ser maior do que zero.")
        Double thighCm,

        @Positive(message = "Circunferência da panturrilha deve ser maior do que zero.")
        Double calfCm
) {
}
