package com.ironcore.domain.auth.model;

import com.ironcore.domain.auth.exception.InvalidAuthSessionException;
import com.ironcore.domain.auth.valueobject.AuthSessionId;
import com.ironcore.domain.user.valueobject.UserId;
import lombok.Getter;

import java.time.Instant;

@Getter
public class AuthSession {

    private final AuthSessionId id;
    private final UserId userId;
    private final Instant createdAt;
    private final Instant expiresAt;
    private Instant revokedAt;
    private String refreshTokenHash;
    private final Long version;

    private AuthSession(AuthSessionId id, UserId userId, Instant createdAt, Instant expiresAt, Instant revokedAt,
                        Long version, String refreshTokenHash) {
        this.id = requireNonNull(id, "Dados obrigatórios da sessão não informados.");
        this.userId = requireNonNull(userId, "Dados obrigatórios da sessão não informados.");
        this.createdAt = requireNonNull(createdAt, "Dados obrigatórios da sessão não informados.");
        this.expiresAt = requiredNonNullAndAfter(expiresAt, "Expiração deve ser informada e posterior à criação.");
        this.revokedAt = validateRevokedAt(
                revokedAt, "Revogação deve ser informada e não pode anteceder a criação."
        );
        this.version = requiredNonNullAndPositive(version, "Versão da sessão inválida.");
        this.refreshTokenHash = refreshTokenHash == null ? null : validateRefreshTokenHash(refreshTokenHash);
    }

    public static AuthSession open(AuthSessionId id, UserId userId, Instant createdAt, Instant expiresAt,
                                   String refreshTokenHash) {
        return new AuthSession(id, userId, createdAt, expiresAt, null, null, validateRefreshTokenHash(refreshTokenHash));
    }

    public static AuthSession restore(AuthSessionId id, UserId userId, Instant createdAt, Instant expiresAt,
                                      Instant revokedAt, Long version, String refreshTokenHash) {
        return new AuthSession(id, userId, createdAt, expiresAt, revokedAt, version, refreshTokenHash);
    }

    public void rotateRefreshToken(String refreshTokenHash, Instant now) {
        if (!isActiveAt(now)) {
            throw new InvalidAuthSessionException("Sessão expirada ou revogada.");
        }
        this.refreshTokenHash = validateRefreshTokenHash(refreshTokenHash);
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

        return revokedAt == null && !instant.isBefore(createdAt) && instant.isBefore(expiresAt);
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
