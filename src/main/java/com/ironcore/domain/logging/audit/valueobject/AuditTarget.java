package com.ironcore.domain.logging.audit.valueobject;

import com.ironcore.domain.logging.audit.enums.AuditTargetType;

public record AuditTarget(AuditTargetType type, Long id) {

    public AuditTarget {
        if (type == null) {
            throw new IllegalArgumentException("Tipo do alvo não pode ser nulo");
        }

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Id do alvo deve ser positivo");
        }
    }
}
