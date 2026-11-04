package com.ironcore.application.auth.usecase;

import com.ironcore.application.auth.port.AccessTokenGenerator;
import com.ironcore.application.auth.port.RefreshTokenGenerator;
import com.ironcore.application.exception.InvalidCredentialsException;
import com.ironcore.domain.auth.model.AuthSession;
import com.ironcore.domain.auth.model.AuthSessionReference;
import com.ironcore.domain.auth.repository.AuthSessionRepository;
import com.ironcore.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.ironcore.application.auth.LoginTestFactory.generatedAccessToken;
import static com.ironcore.domain.auth.AuthSessionTestFactory.sessionId;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshSessionUseCaseTest {
    private static final Instant NOW = Instant.parse("2026-11-04T12:00:00Z");
    private static final String HASH = "a".repeat(64);
    private final AuthSessionRepository sessions = mock(AuthSessionRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final RefreshTokenGenerator refreshTokens = mock(RefreshTokenGenerator.class);
    private final AccessTokenGenerator accessTokens = mock(AccessTokenGenerator.class);
    private final RefreshSessionUseCase useCase = new RefreshSessionUseCase(sessions, users, refreshTokens,
            accessTokens, Clock.fixed(NOW, ZoneOffset.UTC));

    @BeforeEach
    void identifySession() {
        when(refreshTokens.hash("original")).thenReturn(HASH);
        when(sessions.findReferenceByRefreshTokenHash(HASH))
                .thenReturn(Optional.of(new AuthSessionReference(sessionId(), activeUser().getId())));
        when(users.findByIdForUpdate(activeUser().getId())).thenReturn(Optional.of(activeUser()));
    }

    @ParameterizedTest
    @ValueSource(ints = {30, 90})
    void rejectsExactlyAtEitherDeadlineWithoutRotatingOrSaving(int ageInDays) {
        Instant created = NOW.minus(Duration.ofDays(ageInDays));
        var session = AuthSession.restore(sessionId(), activeUser().getId(), created, NOW, null, 0L,
                HASH, created.plus(Duration.ofDays(90)));
        when(sessions.findByRefreshTokenHash(HASH)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> useCase.execute("original")).isInstanceOf(InvalidCredentialsException.class);

        assertThat(session.getExpiresAt()).isEqualTo(NOW);
        assertThat(session.getRefreshTokenHash()).isEqualTo(HASH);
        verify(sessions, never()).save(any());
        verify(refreshTokens, never()).generate();
        verifyNoInteractions(accessTokens);
    }

    @ParameterizedTest
    @ValueSource(ints = {29, 87})
    void renewsOnlySuccessfulRefreshUsingClockAndPassesEffectiveExpiryToTokenGenerator(int ageInDays) {
        Instant created = NOW.minus(Duration.ofDays(ageInDays));
        Instant absolute = created.plus(Duration.ofDays(90));
        var session = AuthSession.restore(sessionId(), activeUser().getId(), created, NOW.plusNanos(1),
                null, 0L, HASH, absolute);
        when(sessions.findByRefreshTokenHash(HASH)).thenReturn(Optional.of(session));
        when(refreshTokens.generate()).thenReturn("next");
        when(refreshTokens.hash("next")).thenReturn("b".repeat(64));
        when(accessTokens.generate(any())).thenReturn(generatedAccessToken());

        var result = useCase.execute("original");

        Instant expected = ageInDays == 29 ? NOW.plus(Duration.ofDays(30)) : absolute;
        assertThat(result.sessionExpiresAt()).isEqualTo(expected);
        assertThat(result.refreshToken()).isEqualTo("next");
        assertThat(session.getAbsoluteExpiresAt()).isEqualTo(absolute);
        verify(sessions).save(session);
        verify(accessTokens).generate(argThat(subject -> subject.sessionExpiresAt().equals(expected)));
    }
}
