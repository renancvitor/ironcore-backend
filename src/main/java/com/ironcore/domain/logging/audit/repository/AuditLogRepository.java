package com.ironcore.domain.logging.audit.repository;

import com.ironcore.domain.logging.audit.model.AuditLog;

public interface AuditLogRepository {
    AuditLog save(AuditLog auditLog);
}
