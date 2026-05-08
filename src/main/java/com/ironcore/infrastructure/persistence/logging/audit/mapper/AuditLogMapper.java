package com.ironcore.infrastructure.persistence.logging.audit.mapper;

import com.ironcore.domain.logging.audit.model.AuditLog;
import com.ironcore.domain.logging.audit.valueobject.AuditAction;
import com.ironcore.domain.logging.audit.valueobject.AuditActor;
import com.ironcore.domain.logging.audit.valueobject.AuditSnapshot;
import com.ironcore.domain.logging.audit.valueobject.AuditTarget;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.logging.audit.entity.AuditLogEntity;

public class AuditLogMapper {

    public static AuditLogEntity toEntity(AuditLog auditLog) {
        try {
            return new AuditLogEntity(
                    auditLog.getId(),
                    auditLog.getActor().userId().value(),
                    auditLog.getActor().email().value(),
                    auditLog.getAction().type(),
                    auditLog.getTarget().type(),
                    auditLog.getTarget().id(),
                    snapshotValue(auditLog.getBeforeState()),
                    snapshotValue(auditLog.getAfterState()),
                    auditLog.getCreatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter audit log de domínio para entidade.", exception);
        }
    }

    public static AuditLog toDomain(AuditLogEntity entity) {
        try {
            return new AuditLog(
                    entity.getId(),
                    new AuditActor(new UserId(entity.getActorUserId()), new Email(entity.getActorEmail())),
                    new AuditAction(entity.getAction()),
                    new AuditTarget(entity.getTargetType(), entity.getTargetId()),
                    snapshot(entity.getBeforeStateJson()),
                    snapshot(entity.getAfterStateJson()),
                    entity.getCreatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter audit log de entidade para domínio.", exception);
        }
    }

    private static String snapshotValue(AuditSnapshot snapshot) {
        return snapshot == null ? null : snapshot.value();
    }

    private static AuditSnapshot snapshot(String value) {
        return value == null ? null : new AuditSnapshot(value);
    }
}
