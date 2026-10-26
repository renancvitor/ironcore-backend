package com.ironcore.domain.workoutsession.workoutsession.model;

import com.ironcore.domain.workoutsession.workoutsession.exception.InvalidWorkoutSessionException;

import java.math.BigDecimal;
import java.time.Instant;

final class SessionValidation {
    private SessionValidation() {}

    static <T> T required(T value, String field) {
        if (value == null) throw new InvalidWorkoutSessionException(field + " é obrigatório.");

        return value;
    }

    static void positive(Integer value, String field) {
        if (value != null && value <= 0)
            throw new InvalidWorkoutSessionException(field + " deve ser positivo.");
    }

    static void decimal(BigDecimal value, String field) {
        if (value != null && (value.signum() < 0 || value.scale() > 2
                || value.compareTo(new BigDecimal("999999.99")) > 0))
            throw new InvalidWorkoutSessionException(field + " deve estar entre 0 e 999999.99, com até duas casas decimais.");
    }

    static void timestamp(Instant value, Instant createdAt) {
        required(value, "Data de atualização");

        if (value.isBefore(createdAt))
            throw new InvalidWorkoutSessionException("Data de atualização anterior à criação.");
    }

}
