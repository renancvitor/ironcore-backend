package com.ironcore.domain.workoutsession.workoutsessionset.model;

import com.ironcore.domain.workoutsession.workoutsessionset.exception.InvalidWorkoutSessionSetException;

import java.math.BigDecimal;
import java.time.Instant;

final class WorkoutSessionSetValidation {
    private WorkoutSessionSetValidation() {}

    static <T> T required(T value, String field) {
        if (value == null) throw new InvalidWorkoutSessionSetException(field + " é obrigatório.");

        return value;
    }

    static void positive(Integer value, String field) {
        if (value != null && value <= 0)
            throw new InvalidWorkoutSessionSetException(field + " deve ser positivo.");
    }

    static void decimal(BigDecimal value, String field) {
        if (value != null && (value.signum() < 0 || value.scale() > 2
                || value.compareTo(new BigDecimal("999999.99")) > 0))
            throw new InvalidWorkoutSessionSetException(field + " deve estar entre 0 e 999999.99, com até duas casas decimais.");
    }

    static void timestamp(Instant value, Instant createdAt) {
        required(value, "Data de atualização");

        if (value.isBefore(createdAt))
            throw new InvalidWorkoutSessionSetException("Data de atualização anterior à criação.");
    }

}
