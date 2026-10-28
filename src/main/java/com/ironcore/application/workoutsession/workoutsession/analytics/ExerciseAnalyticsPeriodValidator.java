package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.application.exception.BusinessRuleViolationException;
import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;

import java.time.Clock;
import java.time.LocalDate;

final class ExerciseAnalyticsPeriodValidator {

    private static final int MAX_PERIOD_MONTHS = 12;

    private ExerciseAnalyticsPeriodValidator() {
    }

    static void validate(LocalDate startDate, LocalDate endDate, Clock clock) {
        if (startDate == null || endDate == null) {
            throw new BusinessRuleViolationException("As datas são obrigatórias.");
        }

        if (startDate.isAfter(endDate)) {
            throw new OperationNotAllowedException("Data inicial não pode ser posterior à data final.");
        }

        if (endDate.isAfter(LocalDate.now(clock.withZone(WorkoutSession.ZONE)))) {
            throw new OperationNotAllowedException("Datas futuras não são permitidas.");
        }

        if (endDate.isAfter(startDate.plusMonths(MAX_PERIOD_MONTHS).minusDays(1))) {
            throw new OperationNotAllowedException("Período máximo permitido é de 12 meses.");
        }
    }
}
