package com.ironcore.interfaces.rest.userbodymetrics.dto.update;

import com.ironcore.interfaces.rest.userbodymetrics.dto.BodyCircumferencesRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateUserBodyMetricsRequest(
        @NotNull(message = "Peso é obrigatório.")
        @Positive(message = "Peso deve ser maior do que zero.")
        Double weightKg,

        @NotNull(message = "Altura é obrigatória.")
        @Positive(message = "Altura deve ser maior do que zero.")
        Double heightCm,

        @Valid
        BodyCircumferencesRequest circumferences,

        String notes
) {
}
