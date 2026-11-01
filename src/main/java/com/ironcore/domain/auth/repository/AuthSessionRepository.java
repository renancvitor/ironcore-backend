package com.ironcore.domain.auth.repository;

import com.ironcore.domain.auth.model.AuthSession;
import com.ironcore.domain.auth.valueobject.AuthSessionId;
import com.ironcore.domain.user.valueobject.UserId;

import java.time.Instant;
import java.util.Optional;

public interface AuthSessionRepository {

    AuthSession save(AuthSession session);

    Optional<AuthSession> findByIdAndUserId(AuthSessionId id, UserId userId);

    int revokeAllByUserId(UserId userId, Instant revokedAt);
}
