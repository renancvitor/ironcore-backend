package com.ironcore.domain.logging.audit.valueobject;

import com.ironcore.domain.logging.audit.enums.AuditActionType;

import java.util.Objects;

public record AuditAction(AuditActionType type) {

    public AuditAction {
        Objects.requireNonNull(type, "Tipo da ação não pode ser nulo");
    }
}
