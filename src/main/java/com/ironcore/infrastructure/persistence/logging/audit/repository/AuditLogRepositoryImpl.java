package com.ironcore.infrastructure.persistence.logging.audit.repository;

import com.ironcore.domain.logging.audit.model.AuditLog;
import com.ironcore.domain.logging.audit.repository.AuditLogRepository;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.logging.audit.entity.AuditLogEntity;
import com.ironcore.infrastructure.persistence.logging.audit.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogJpaRepository auditLogJpaRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogEntity entity;
        try {
            entity = Objects.requireNonNull(
                    AuditLogMapper.toEntity(auditLog),
                    "AuditLogMapper retornou entidade nula."
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter audit log de domínio para entidade.", exception);
        }

        AuditLogEntity savedEntity;
        try {
            savedEntity = Objects.requireNonNull(
                    auditLogJpaRepository.save(entity),
                    "AuditLogJpaRepository retornou entidade nula após persistência."
            );
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao persistir audit log.", exception);
        }

        try {
            return AuditLogMapper.toDomain(savedEntity);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter audit log persistido para domínio.", exception);
        }
    }
}
