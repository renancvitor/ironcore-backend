package com.ironcore.domain.auth.model;

import com.ironcore.domain.auth.exception.InvalidAuthSessionException;
import com.ironcore.domain.auth.valueobject.AuthSessionId;
import com.ironcore.domain.user.valueobject.UserId;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;

@Getter
public class AuthSession {

    public static final Duration INACTIVITY_TIMEOUT = Duration.ofDays(30);
    public static final Duration ABSOLUTE_TIMEOUT = Duration.ofDays(90);

    private final AuthSessionId id;
    private final UserId userId;
    private final Instant createdAt;
    private Instant expiresAt;
    private final Instant absoluteExpiresAt;
    private Instant revokedAt;
    private String refreshTokenHash;
    private final Long version;

    private AuthSession(AuthSessionId id, UserId userId, Instant createdAt, Instant expiresAt, Instant revokedAt,
                        Long version, String refreshTokenHash, Instant absoluteExpiresAt) {
        this.id = requireNonNull(id, "Dados obrigatórios da sessão não informados.");
        this.userId = requireNonNull(userId, "Dados obrigatórios da sessão não informados.");
        this.createdAt = requireNonNull(createdAt, "Dados obrigatórios da sessão não informados.");
        this.expiresAt = requiredNonNullAndAfter(expiresAt, "Expiração deve ser informada e posterior à criação.");
        this.absoluteExpiresAt = requiredNonNullAndAfter(absoluteExpiresAt, "Limite absoluto inválido.");
        if (expiresAt.isAfter(absoluteExpiresAt) || absoluteExpiresAt.isAfter(createdAt.plus(ABSOLUTE_TIMEOUT))) {
            throw new InvalidAuthSessionException("Expiração excede o limite absoluto da sessão.");
        }
        this.revokedAt = validateRevokedAt(
                revokedAt, "Revogação deve ser informada e não pode anteceder a criação."
        );
        this.version = requiredNonNullAndPositive(version, "Versão da sessão inválida.");
        this.refreshTokenHash = refreshTokenHash == null ? null : validateRefreshTokenHash(refreshTokenHash);
    }

    public static AuthSession open(AuthSessionId id, UserId userId, Instant createdAt, String refreshTokenHash) {
        if (createdAt == null) {
            throw new InvalidAuthSessionException("Criação da sessão não informada.");
        }
        return new AuthSession(id, userId, createdAt, createdAt.plus(INACTIVITY_TIMEOUT), null, null,
                validateRefreshTokenHash(refreshTokenHash), createdAt.plus(ABSOLUTE_TIMEOUT));
    }

    public static AuthSession restore(AuthSessionId id, UserId userId, Instant createdAt, Instant expiresAt,
                                      Instant revokedAt, Long version, String refreshTokenHash, Instant absoluteExpiresAt) {
        return new AuthSession(id, userId, createdAt, expiresAt, revokedAt, version, refreshTokenHash, absoluteExpiresAt);
    }

    public void rotateRefreshToken(String refreshTokenHash, Instant now) {
        if (!isActiveAt(now)) {
            throw new InvalidAuthSessionException("Sessão expirada ou revogada.");
        }
        this.refreshTokenHash = validateRefreshTokenHash(refreshTokenHash);
        Instant inactivityExpiresAt = now.plus(INACTIVITY_TIMEOUT);
        this.expiresAt = inactivityExpiresAt.isBefore(absoluteExpiresAt) ? inactivityExpiresAt : absoluteExpiresAt;
    }

    private static String validateRefreshTokenHash(String hash) {
        if (hash == null || !hash.matches("[0-9a-f]{64}")) {
            throw new InvalidAuthSessionException("Hash de refresh token inválido.");
        }
        return hash;
    }

    public boolean isActiveAt(Instant instant) {
        if (instant == null) {
            throw new InvalidAuthSessionException("Instante de validação não informado.");
        }

        return revokedAt == null && !instant.isBefore(createdAt) && instant.isBefore(expiresAt)
                && instant.isBefore(absoluteExpiresAt);
    }

    public void revoke(Instant revokedAt) {
        if (this.revokedAt != null) {
            return;
        }

        if (revokedAt == null || revokedAt.isBefore(createdAt)) {
            throw new InvalidAuthSessionException("Data de revogação inválida.");
        }

        this.revokedAt = revokedAt;
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidAuthSessionException(message);
        }

        return value;
    }

    private Instant requiredNonNullAndAfter(Instant value, String message) {
        if (value == null || !value.isAfter(createdAt)) {
            throw new InvalidAuthSessionException(message);
        }

        return value;
    }

    private Instant validateRevokedAt(Instant value, String message) {
        if (value != null && value.isBefore(createdAt)) {
            throw new InvalidAuthSessionException(message);
        }

        return value;
    }

    private Long requiredNonNullAndPositive(Long value, String message) {
        if (value != null && value < 0) {
            throw new InvalidAuthSessionException(message);
        }

        return value;
    }
}
