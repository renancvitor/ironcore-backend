package com.ironcore.domain.userbodymetrics.valueobject;

import com.ironcore.domain.userbodymetrics.exception.InvalidBodyMetricException;

public record UserBodyMetricsId(Long value) {

    public UserBodyMetricsId {
        if (value == null || value <= 0) {
            throw new InvalidBodyMetricException("Id das métricas corporais do usuário deve ser positivo.");
        }
    }
}
