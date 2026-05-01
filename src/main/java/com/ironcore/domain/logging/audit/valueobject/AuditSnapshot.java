package com.ironcore.domain.logging.audit.valueobject;

public record AuditSnapshot(String value) {

    public AuditSnapshot {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Snapshot da auditoria não pode ser nulo ou vazio");
        }

        value = value.trim();
    }
}
