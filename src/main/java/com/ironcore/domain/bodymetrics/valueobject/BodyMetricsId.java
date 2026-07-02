package com.ironcore.domain.bodymetrics.valueobject;

import com.ironcore.domain.bodymetrics.exception.InvalidPersonBodyMetricsException;

public record BodyMetricsId(Long value) {

    public BodyMetricsId {
        if (value == null || value <= 0) {
            throw new InvalidPersonBodyMetricsException("Id das métricas corporais da pessoa deve ser positivo.");
        }
    }
}
