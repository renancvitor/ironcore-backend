package com.ironcore.application.workoutsession.workoutsession.expire;

import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpireWorkoutSessionUseCaseTest {

    @Mock
    private WorkoutSessionRepository repository;

    @Mock
    private Clock clock;

    private ExpireWorkoutSessionUseCase useCase;
    private final ExpireWorkoutSessionCommand command = new ExpireWorkoutSessionCommand(OWNER);

    @BeforeEach
    void setUp() {
        useCase = new ExpireWorkoutSessionUseCase(repository, clock);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, 172800})
    void shouldExpireAtOrAfterDeadlineAndPreserveAllRecordedData(long delaySeconds) {
        WorkoutSession session = session();
        session.addSet(OWNER, ACTIVITY_ID, 1, null, null, null, "Relato", START.plusSeconds(1));
        session.updateActivityExecution(OWNER, ACTIVITY_ID, 15, null, "Atividade", START.plusSeconds(2));
        session.updateNotes(OWNER, "Sessão", START.plusSeconds(3));
        session = restoredSession(session);
        WorkoutSession before = restoredSession(session);
        Instant now = EXPIRY.plusSeconds(delaySeconds);
        when(repository.findInProgressByPersonId(OWNER)).thenReturn(Optional.of(session));
        when(clock.instant()).thenReturn(now);
        when(repository.save(session)).thenReturn(session);

        ExpireWorkoutSessionResult result = useCase.execute(command).orElseThrow();

        assertThat(result.id()).isEqualTo(session.getId());
        assertThat(result.finishedAt()).isEqualTo(EXPIRY);
        assertThat(result.updatedAt()).isEqualTo(now);
        assertThat(session.getStatus()).isEqualTo(WorkoutSessionStatus.CLOSED);
        assertThat(session.getCloseReason()).isEqualTo(WorkoutSessionCloseReason.AUTO_EXPIRED);
        assertThat(session).usingRecursiveComparison()
                .ignoringFields("status", "closeReason", "finishedAt", "updatedAt").isEqualTo(before);
        verify(repository).save(session);
    }

    @Test
    void shouldNotPersistBeforeDeadline() {
        WorkoutSession session = session();
        when(repository.findInProgressByPersonId(OWNER)).thenReturn(Optional.of(session));
        when(clock.instant()).thenReturn(EXPIRY.minusNanos(1));

        assertThat(useCase.execute(command)).isEmpty();
        assertThat(session.getStatus()).isEqualTo(WorkoutSessionStatus.IN_PROGRESS);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldNotWriteAgainAfterSuccessfulExpiration() {
        WorkoutSession session = session();
        when(repository.findInProgressByPersonId(OWNER)).thenReturn(Optional.of(session), Optional.empty());
        when(clock.instant()).thenReturn(EXPIRY.plus(Duration.ofDays(2)));
        when(repository.save(session)).thenReturn(session);

        assertThat(useCase.execute(command)).isPresent();
        assertThat(useCase.execute(command)).isEmpty();

        verify(repository).save(session);
        assertThat(session.getActivities()).allSatisfy(activity -> {
            assertThat(activity.getSets()).isEmpty();
            assertThat(activity.getActualDurationMinutes()).isNull();
            assertThat(activity.getActualDistanceKm()).isNull();
        });
    }

    @Test
    void shouldSkipPersonWithoutOpenSession() {
        when(repository.findInProgressByPersonId(OWNER)).thenReturn(Optional.empty());

        assertThat(useCase.execute(command)).isEmpty();

        verify(repository, never()).save(any());
        verifyNoInteractions(clock);
    }
}
