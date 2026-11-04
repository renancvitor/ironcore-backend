package com.ironcore.application.auth.usecase;

import com.ironcore.domain.auth.model.AuthSession;
import com.ironcore.domain.auth.repository.AuthSessionRepository;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.security.token.SecureRefreshTokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.ironcore.domain.auth.AuthSessionTestFactory.sessionId;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock
    private AuthSessionRepository sessions;

    @Mock
    private UserRepository users;

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-01T12:00:00Z"), ZoneOffset.UTC);
    private LogoutUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LogoutUseCase(sessions, clock, users, new SecureRefreshTokenGenerator());
    }

    @Test
    void revokesOnlyTheSessionBelongingToTheAuthenticatedUser() {
        UserId owner = new UserId(1L);
        when(users.findByIdForUpdate(owner)).thenReturn(Optional.of(activeUser()));
        AuthSession session = AuthSession.open(sessionId(), owner,
                clock.instant().minusSeconds(60), "a".repeat(64));
        when(sessions.findByIdAndUserId(sessionId(), owner)).thenReturn(Optional.of(session));

        useCase.execute(new LogoutCommand(owner, sessionId(), null));

        assertThat(session.isActiveAt(clock.instant())).isFalse();
        assertThat(session.getRevokedAt()).isEqualTo(clock.instant());
        verify(sessions).save(session);
        verify(sessions, never()).revokeAllByUserId(any(), any());
    }

    @Test
    void doesNotRevokeAnotherUsersSession() {
        UserId otherUser = new UserId(2L);
        when(users.findByIdForUpdate(otherUser)).thenReturn(Optional.of(activeUser()));
        when(sessions.findByIdAndUserId(sessionId(), otherUser)).thenReturn(Optional.empty());

        useCase.execute(new LogoutCommand(otherUser, sessionId(), null));

        verify(sessions, never()).save(any());
        verify(sessions, never()).revokeAllByUserId(any(), any());
    }

    @Test
    void doesNothingWhenTheUserNoLongerExists() {
        UserId owner = new UserId(1L);
        when(users.findByIdForUpdate(owner)).thenReturn(Optional.empty());

        useCase.execute(new LogoutCommand(owner, sessionId(), null));

        verifyNoInteractions(sessions);
    }
}
