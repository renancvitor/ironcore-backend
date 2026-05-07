package com.ironcore.domain.logging.audit.valueobject;

import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.logging.audit.exception.InvalidAuditLogException;

public record AuditTarget(AuditTargetType type, Long id) {

    public AuditTarget {
        if (type == null) {
            throw new InvalidAuditLogException("Tipo do alvo não pode ser nulo");
        }

        if (id == null || id <= 0) {
            throw new InvalidAuditLogException("Id do alvo deve ser positivo");
        }
    }
}
