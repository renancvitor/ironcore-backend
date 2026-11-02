package com.ironcore.infrastructure.persistence.auth.mapper;

import com.ironcore.domain.auth.model.AuthSession;
import com.ironcore.domain.auth.valueobject.AuthSessionId;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.auth.entity.AuthSessionEntity;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;

public final class AuthSessionMapper {

    private AuthSessionMapper() {
    }

    public static AuthSessionEntity toEntity(AuthSession session, UserEntity user) {
        try {
            if (!session.getUserId().value().equals(user.getId())) {
                throw new IllegalArgumentException("Proprietário da sessão divergente.");
            }

            AuthSessionEntity entity = new AuthSessionEntity();
            entity.setId(session.getId().value());
            entity.setUser(user);
            entity.setCreatedAt(session.getCreatedAt());
            entity.setExpiresAt(session.getExpiresAt());
            entity.setRevokedAt(session.getRevokedAt());
            entity.setVersion(session.getVersion());
            entity.setRefreshTokenHash(session.getRefreshTokenHash());

            return entity;
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter sessão de domínio para entidade.", exception);
        }
    }

    public static AuthSession toDomain(AuthSessionEntity entity) {
        try {
            return AuthSession.restore(
                    new AuthSessionId(entity.getId()),
                    new UserId(entity.getUser().getId()),
                    entity.getCreatedAt(),
                    entity.getExpiresAt(),
                    entity.getRevokedAt(),
                    entity.getVersion(),
                    entity.getRefreshTokenHash()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter sessão de entidade para domínio.", exception);
        }
    }
}
