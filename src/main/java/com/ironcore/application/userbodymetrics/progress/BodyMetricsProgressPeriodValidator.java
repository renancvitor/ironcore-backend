package com.ironcore.application.userbodymetrics.progress;

import com.ironcore.application.exception.BusinessRuleViolationException;
import com.ironcore.application.exception.OperationNotAllowedException;

import java.time.LocalDate;

final class BodyMetricsProgressPeriodValidator {

    private static final int MAX_PROGRESS_PERIOD_MONTHS = 12;

    static void validate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessRuleViolationException("As datas são obrigatórias.");
        }

        if (startDate.isAfter(endDate)) {
            throw new OperationNotAllowedException("Data inicial não pode ser maior do que data final.");
        }

        LocalDate maxEndDate = startDate.plusMonths(MAX_PROGRESS_PERIOD_MONTHS).minusDays(1);

        if (endDate.isAfter(maxEndDate)) {
            throw new OperationNotAllowedException(
                    "Período máximo permitido é de " + MAX_PROGRESS_PERIOD_MONTHS + " meses."
            );
        }
    }
}
