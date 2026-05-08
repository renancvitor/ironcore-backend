package com.ironcore.domain.logging.audit.valueobject;

import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.exception.InvalidAuditLogException;

public record AuditAction(AuditActionType type) {

    public AuditAction {
        if (type == null) {
            throw new InvalidAuditLogException("Tipo da ação não pode ser nulo");
        }
    }
}
