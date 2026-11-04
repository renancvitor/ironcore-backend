package com.ironcore.domain.auth;

import com.ironcore.domain.auth.exception.InvalidAuthSessionException;
import com.ironcore.domain.auth.model.AuthSession;
import com.ironcore.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static com.ironcore.domain.auth.AuthSessionTestFactory.sessionId;
import static org.assertj.core.api.Assertions.*;

class AuthSessionExpirationTest {
    private static final Instant CREATED = Instant.parse("2026-11-04T12:00:00Z");
    private static final String HASH = "a".repeat(64);
    private static final String NEXT_HASH = "b".repeat(64);

    private AuthSession session() {
        return AuthSession.open(sessionId(), new UserId(1L), CREATED, HASH);
    }

    @Test
    void opensWithThirtyDaysOfInactivityAndNinetyDaysAbsolute() {
        var session = session();
        assertThat(session.getExpiresAt()).isEqualTo(CREATED.plus(Duration.ofDays(30)));
        assertThat(session.getAbsoluteExpiresAt()).isEqualTo(CREATED.plus(Duration.ofDays(90)));
        assertThat(session.isActiveAt(CREATED.minusNanos(1))).isFalse();
        assertThat(session.isActiveAt(CREATED)).isTrue();
        assertThat(session.isActiveAt(session.getExpiresAt().minusNanos(1))).isTrue();
        assertThat(session.isActiveAt(session.getExpiresAt())).isFalse();
        assertThat(session.isActiveAt(session.getExpiresAt().plusNanos(1))).isFalse();
    }

    @Test
    void rotatesJustBeforeExpiryAndRenewsInactivityWithoutMovingTheAbsoluteLimit() {
        var session = session();
        Instant now = session.getExpiresAt().minusNanos(1);
        session.rotateRefreshToken(NEXT_HASH, now);
        assertThat(session.getRefreshTokenHash()).isEqualTo(NEXT_HASH);
        assertThat(session.getExpiresAt()).isEqualTo(now.plus(Duration.ofDays(30)));
        assertThat(session.getAbsoluteExpiresAt()).isEqualTo(CREATED.plus(Duration.ofDays(90)));
    }

    @Test
    void repeatedActivityStopsExactlyAtNinetyDays() {
        var session = session();
        for (int day : new int[]{29, 58, 87}) {
            session.rotateRefreshToken(NEXT_HASH, CREATED.plus(Duration.ofDays(day)));
        }
        Instant limit = CREATED.plus(Duration.ofDays(90));
        assertThat(session.getExpiresAt()).isEqualTo(limit);
        assertThat(session.isActiveAt(limit.minusNanos(1))).isTrue();
        session.rotateRefreshToken(HASH, limit.minusNanos(1));
        assertThat(session.getExpiresAt()).isEqualTo(limit);
        assertThat(session.isActiveAt(limit)).isFalse();
        assertThat(session.isActiveAt(limit.plusNanos(1))).isFalse();
        assertThatThrownBy(() -> session.rotateRefreshToken(NEXT_HASH, limit))
                .isInstanceOf(InvalidAuthSessionException.class);
    }

    @Test
    void expiredRevokedAndInvalidRotationsDoNotChangeDeadlinesOrHashes() {
        var session = session();
        Instant expiry = session.getExpiresAt();
        assertThatThrownBy(() -> session.rotateRefreshToken(NEXT_HASH, expiry))
                .isInstanceOf(InvalidAuthSessionException.class);
        assertThatThrownBy(() -> session.rotateRefreshToken("invalid", CREATED.plusSeconds(1)))
                .isInstanceOf(InvalidAuthSessionException.class);
        session.revoke(CREATED.plusSeconds(1));
        assertThatThrownBy(() -> session.rotateRefreshToken(NEXT_HASH, CREATED.plusSeconds(2)))
                .isInstanceOf(InvalidAuthSessionException.class);
        assertThat(session.getExpiresAt()).isEqualTo(expiry);
        assertThat(session.getRefreshTokenHash()).isEqualTo(HASH);
    }

    @Test
    void legacySessionNeverExtendsItsOriginalDeadline() {
        Instant deadline = CREATED.plus(Duration.ofDays(7));
        var session = AuthSession.restore(sessionId(), new UserId(1L), CREATED, deadline, null, 1L, HASH, deadline);
        session.rotateRefreshToken(NEXT_HASH, deadline.minusSeconds(1));
        assertThat(session.getExpiresAt()).isEqualTo(deadline);
        assertThat(session.isActiveAt(deadline)).isFalse();
    }

    @Test
    void rejectsPersistenceStateExceedingAbsoluteLimits() {
        Instant limit = CREATED.plus(Duration.ofDays(90));
        assertThatThrownBy(() -> AuthSession.restore(sessionId(), new UserId(1L), CREATED,
                limit.plusSeconds(1), null, 1L, HASH, limit)).isInstanceOf(InvalidAuthSessionException.class);
        assertThatThrownBy(() -> AuthSession.restore(sessionId(), new UserId(1L), CREATED,
                limit, null, 1L, HASH, limit.plusSeconds(1))).isInstanceOf(InvalidAuthSessionException.class);
    }
}
