package com.ironcore.domain.userbodymetrics.valueobject;

import com.ironcore.domain.userbodymetrics.exception.InvalidUserBodyMetricsException;

public record UserBodyMetricsId(Long value) {

    public UserBodyMetricsId {
        if (value == null || value <= 0) {
            throw new InvalidUserBodyMetricsException("Id das métricas corporais do usuário deve ser positivo.");
        }
    }
}
