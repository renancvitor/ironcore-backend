package com.ironcore.infrastructure.persistence.auth.repository;

import com.ironcore.application.auth.port.AuthenticationAttemptStore;
import com.ironcore.infrastructure.exception.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostgresAuthenticationAttemptStore implements AuthenticationAttemptStore {

    private final JdbcTemplate jdbc;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Instant> registerAttempt(String subject, Instant now, Duration window, int limit) {
        String key = hash(subject);
        Timestamp cutoff = Timestamp.from(now.minus(window));
        try {
            return jdbc.queryForObject("""
                    INSERT INTO auth_attempt_limits (subject_hash, window_started_at, attempts)
                    VALUES (?, ?, 1)
                    ON CONFLICT (subject_hash) DO UPDATE SET
                        attempts = CASE WHEN auth_attempt_limits.window_started_at <= ? THEN 1
                            ELSE LEAST(auth_attempt_limits.attempts, ?) + 1 END,
                        window_started_at = CASE WHEN auth_attempt_limits.window_started_at <= ? THEN EXCLUDED.window_started_at
                            ELSE auth_attempt_limits.window_started_at END
                    RETURNING attempts, window_started_at
                    """, (row, index) -> row.getInt("attempts") > limit
                    ? Optional.of(row.getTimestamp("window_started_at").toInstant().plus(window)) : Optional.<Instant>empty(),
                    key, Timestamp.from(now), cutoff, limit, cutoff);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao registrar tentativa de autenticação.", exception);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Reservation reserveAttempt(String subject, Instant now, Duration window, int limit) {
        String key = hash(subject);
        Timestamp cutoff = Timestamp.from(now.minus(window));
        try {
            var accepted = jdbc.query("""
                    INSERT INTO auth_attempt_limits (subject_hash, window_started_at, attempts)
                    VALUES (?, ?, 1)
                    ON CONFLICT (subject_hash) DO UPDATE SET
                        attempts = CASE WHEN auth_attempt_limits.window_started_at <= ? THEN 1
                            ELSE auth_attempt_limits.attempts + 1 END,
                        window_started_at = CASE WHEN auth_attempt_limits.window_started_at <= ? THEN EXCLUDED.window_started_at
                            ELSE auth_attempt_limits.window_started_at END
                    WHERE auth_attempt_limits.window_started_at <= ? OR auth_attempt_limits.attempts < ?
                    RETURNING window_started_at
                    """, (row, index) -> row.getTimestamp("window_started_at").toInstant(),
                    key, Timestamp.from(now), cutoff, cutoff, cutoff, limit);
            if (!accepted.isEmpty()) {
                return new Reservation(accepted.getFirst(), true);
            }
            Instant startedAt = jdbc.queryForObject(
                    "SELECT window_started_at FROM auth_attempt_limits WHERE subject_hash = ?",
                    (row, index) -> row.getTimestamp("window_started_at").toInstant(), key);
            return new Reservation(startedAt, false);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao reservar tentativa de autenticação.", exception);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releaseAttempt(String subject, Instant windowStartedAt) {
        try {
            jdbc.update("""
                    UPDATE auth_attempt_limits SET attempts = attempts - 1
                    WHERE subject_hash = ? AND window_started_at = ? AND attempts > 0
                    """, hash(subject), Timestamp.from(windowStartedAt));
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao liberar tentativa de autenticação.", exception);
        }
    }

    @Override
    @Transactional
    public void deleteExpiredBefore(Instant cutoff) {
        try {
            jdbc.update("DELETE FROM auth_attempt_limits WHERE window_started_at <= ?", Timestamp.from(cutoff));
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao remover limites de autenticação expirados.", exception);
        }
    }

    private String hash(String subject) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(subject.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível.", exception);
        }
    }
}
