package com.ironcore.interfaces.rest.userbodymetrics.dto.progress;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record BodyMetricsProgressChartRequest(
        @NotNull(message = "Data inicial é obrigatória.")
        @PastOrPresent(message = "Data inicial não pode ser futura.")
        LocalDate startDate,

        @NotNull(message = "Data final é obrigatória.")
        @PastOrPresent(message = "Data final não pode ser futura.")
        LocalDate endDate
) {
}
