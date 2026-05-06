package com.ironcore.domain.logging.audit.valueobject;

import com.ironcore.domain.logging.audit.enums.AuditActionType;

public record AuditAction(AuditActionType type) {

    public AuditAction {
        if (type == null) {
            throw new IllegalArgumentException("Tipo da ação não pode ser nulo");
        }
    }
}
