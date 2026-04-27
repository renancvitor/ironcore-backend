package com.ironcore.domain.userbodymetrics.valueobject;

public record UserBodyMetricsId(Long value) {

    public UserBodyMetricsId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Id das métricas corporais do usuário deve ser positivo");
        }
    }
}
