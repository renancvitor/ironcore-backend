package com.ironcore.application.auth.usecase;

import com.ironcore.application.auth.service.AuthSessionIssuer;
import com.ironcore.application.exception.InvalidCredentialsException;
import com.ironcore.application.exception.RefreshConflictException;
import com.ironcore.domain.auth.model.AuthSession;
import com.ironcore.domain.auth.repository.AuthSessionRepository;
import com.ironcore.domain.auth.valueobject.AuthSessionId;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.infrastructure.security.token.SecureRefreshTokenGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.application.auth.LoginTestFactory.generatedAccessToken;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {
    @Mock AuthSessionRepository sessions;
    @Mock UserRepository users;
    @Mock AuthSessionIssuer issuer;
    final Clock clock = Clock.fixed(Instant.parse("2026-10-31T12:00:00Z"), ZoneOffset.UTC);
    final SecureRefreshTokenGenerator tokens = new SecureRefreshTokenGenerator();

    RefreshTokenUseCase useCase() { return new RefreshTokenUseCase(sessions, users, tokens, issuer, clock); }

    @Test
    void issuesOnlyAfterLockingOwnerAndCheckingCurrentCredential() {
        var user = activeUser();
        var id = new AuthSessionId(UUID.randomUUID());
        var session = AuthSession.open(id, user.getId(), clock.instant().minusSeconds(20), clock.instant().plusSeconds(300));
        var token = tokens.generate(id);
        session.rotateRefreshCredential(tokens.hash(token), clock.instant().minusSeconds(10));
        when(sessions.findUserIdById(id)).thenReturn(Optional.of(user.getId()));
        when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(sessions.findByIdAndUserId(id, user.getId())).thenReturn(Optional.of(session));
        var result = generatedAccessToken();
        when(issuer.renew(user, session)).thenReturn(result);
        assertThat(useCase().execute(token)).isSameAs(result);
        var order = inOrder(sessions, users, issuer);
        order.verify(sessions).findUserIdById(id);
        order.verify(users).findByIdForUpdate(user.getId());
        order.verify(sessions).findByIdAndUserId(id, user.getId());
        order.verify(issuer).renew(user, session);
    }

    @Test
    void consumedCredentialNeverIssuesTokensAndDoesNotRevokeCurrentSession() {
        var user = activeUser();
        var id = new AuthSessionId(UUID.randomUUID());
        var session = AuthSession.open(id, user.getId(), clock.instant().minusSeconds(30), clock.instant().plusSeconds(300));
        var old = tokens.generate(id);
        session.rotateRefreshCredential(tokens.hash(old), clock.instant().minusSeconds(20));
        session.rotateRefreshCredential(tokens.hash(tokens.generate(id)), clock.instant().minusSeconds(1));
        when(sessions.findUserIdById(id)).thenReturn(Optional.of(user.getId()));
        when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(sessions.findByIdAndUserId(id, user.getId())).thenReturn(Optional.of(session));
        assertThatThrownBy(() -> useCase().execute(old)).isInstanceOf(RefreshConflictException.class);
        assertThat(session.getRevokedAt()).isNull();
        verifyNoInteractions(issuer);
        verify(sessions, never()).save(any());
    }

    @Test
    void malformedCredentialsCannotSelectSessionOrLockUser() {
        assertThatThrownBy(() -> useCase().execute("invalid")).isInstanceOf(InvalidCredentialsException.class);
        verifyNoInteractions(sessions, users, issuer);
    }
}
