package com.ironcore.infrastructure.persistence.logging.audit.repository;

import com.ironcore.infrastructure.persistence.logging.audit.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long> {
}
