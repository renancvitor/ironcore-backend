package com.ironcore.infrastructure.persistence.logging.audit.repository;

import com.ironcore.domain.logging.audit.model.AuditLog;
import com.ironcore.domain.logging.audit.repository.AuditLogRepository;
import com.ironcore.infrastructure.persistence.logging.audit.entity.AuditLogEntity;
import com.ironcore.infrastructure.persistence.logging.audit.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogJpaRepository auditLogJpaRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogEntity entity = AuditLogMapper.toEntity(auditLog);
        AuditLogEntity savedEntity = auditLogJpaRepository.save(entity);
        return AuditLogMapper.toDomain(savedEntity);
    }
}
