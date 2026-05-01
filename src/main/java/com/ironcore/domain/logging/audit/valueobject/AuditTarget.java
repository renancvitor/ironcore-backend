package com.ironcore.domain.logging.audit.valueobject;

import com.ironcore.domain.logging.audit.enums.AuditTargetType;

import java.util.Objects;

public record AuditTarget(AuditTargetType type, Long id) {

    public AuditTarget {
        Objects.requireNonNull(type, "Tipo do alvo não pode ser nulo");

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Id do alvo deve ser positivo");
        }
    }
}
