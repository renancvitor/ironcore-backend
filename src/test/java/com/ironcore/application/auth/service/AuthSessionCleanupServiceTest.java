package com.ironcore.application.auth.service;

import com.ironcore.domain.auth.repository.AuthSessionRepository;
import com.ironcore.infrastructure.exception.PersistenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AuthSessionCleanupServiceTest {

    @ParameterizedTest
    @ValueSource(strings = {"UTC", "America/Sao_Paulo", "Pacific/Auckland"})
    void shouldRetainFifteenDaysFromTheCurrentInstantRegardlessOfClockZone(String zone) {
        AuthSessionRepository repository = mock(AuthSessionRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-11-03T01:30:00.123456Z"), ZoneId.of(zone));
        AuthSessionCleanupService service = new AuthSessionCleanupService(repository, clock);

        service.deleteExpiredBefore();

        verify(repository).deleteExpiredBefore(Instant.parse("2026-10-19T01:30:00.123456Z"));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldPropagatePersistenceFailureToTheScheduler() {
        AuthSessionRepository repository = mock(AuthSessionRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC);
        PersistenceException failure = new PersistenceException("Unavailable");
        doThrow(failure).when(repository).deleteExpiredBefore(any());

        assertThatThrownBy(() -> new AuthSessionCleanupService(repository, clock).deleteExpiredBefore())
                .isSameAs(failure);
    }
}
