package com.ironcore.domain.logging.audit.valueobject;

import com.ironcore.domain.logging.audit.exception.InvalidAuditLogException;

public record AuditSnapshot(String value) {

    public AuditSnapshot {
        if (value == null || value.isBlank()) {
            throw new InvalidAuditLogException("Snapshot da auditoria não pode ser nulo ou vazio");
        }

        value = value.trim();
    }
}
