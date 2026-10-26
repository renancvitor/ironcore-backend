package com.ironcore.domain.workoutsession.workoutsessionactivity.model;

import com.ironcore.domain.workoutsession.workoutsessionactivity.exception.InvalidWorkoutSessionActivityException;

import java.math.BigDecimal;
import java.time.Instant;

final class WorkoutSessionActivityValidation {
    private WorkoutSessionActivityValidation() {}

    static <T> T required(T value, String field) {
        if (value == null) throw new InvalidWorkoutSessionActivityException(field + " é obrigatório.");

        return value;
    }

    static void positive(Integer value, String field) {
        if (value != null && value <= 0)
            throw new InvalidWorkoutSessionActivityException(field + " deve ser positivo.");
    }

    static void decimal(BigDecimal value, String field) {
        if (value != null && (value.signum() < 0 || value.scale() > 2
                || value.compareTo(new BigDecimal("999999.99")) > 0))
            throw new InvalidWorkoutSessionActivityException(field + " deve estar entre 0 e 999999.99, com até duas casas decimais.");
    }

    static void timestamp(Instant value, Instant createdAt) {
        required(value, "Data de atualização");

        if (value.isBefore(createdAt))
            throw new InvalidWorkoutSessionActivityException("Data de atualização anterior à criação.");
    }

}
