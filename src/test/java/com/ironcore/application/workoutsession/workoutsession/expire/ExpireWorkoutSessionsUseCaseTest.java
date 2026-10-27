package com.ironcore.application.workoutsession.workoutsession.expire;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import com.ironcore.infrastructure.exception.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpireWorkoutSessionsUseCaseTest {

    @Mock
    private WorkoutSessionRepository repository;

    @Mock
    private ExpireWorkoutSessionUseCase expireWorkoutSessionUseCase;

    private ExpireWorkoutSessionsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ExpireWorkoutSessionsUseCase(repository, expireWorkoutSessionUseCase);
    }

    @Test
    void shouldAdvanceCursorAfterFailureAndProcessFollowingPages() {
        List<PersonId> first = LongStream.rangeClosed(1, 100).mapToObj(PersonId::new).toList();
        PersonId last = new PersonId(101L);
        when(repository.findInProgressPersonIdsAfter(null, 100)).thenReturn(first);
        when(repository.findInProgressPersonIdsAfter(first.getLast(), 100)).thenReturn(List.of(last));
        when(repository.findInProgressPersonIdsAfter(last, 100)).thenReturn(List.of());
        when(expireWorkoutSessionUseCase.execute(new ExpireWorkoutSessionCommand(first.getFirst())))
                .thenThrow(new PersistenceException("Falha isolada"));
        when(expireWorkoutSessionUseCase.execute(new ExpireWorkoutSessionCommand(last)))
                .thenReturn(Optional.of(new ExpireWorkoutSessionResult(session().getId(), EXPIRY, EXPIRY)));

        useCase.execute();

        for (PersonId personId : first) {
            verify(expireWorkoutSessionUseCase).execute(new ExpireWorkoutSessionCommand(personId));
        }
        verify(expireWorkoutSessionUseCase).execute(new ExpireWorkoutSessionCommand(last));
        verify(repository).findInProgressPersonIdsAfter(last, 100);
        verifyNoMoreInteractions(repository, expireWorkoutSessionUseCase);
    }

    @Test
    void shouldRestartFromBeginningOnEveryRun() {
        when(repository.findInProgressPersonIdsAfter(null, 100)).thenReturn(List.of(OWNER), List.of());
        when(repository.findInProgressPersonIdsAfter(OWNER, 100)).thenReturn(List.of());

        useCase.execute();
        useCase.execute();

        verify(repository, times(2)).findInProgressPersonIdsAfter(null, 100);
        verify(expireWorkoutSessionUseCase).execute(new ExpireWorkoutSessionCommand(OWNER));
    }

    @Test
    void shouldRetrySelectionOnNextRunAfterDatabaseFailure() {
        when(repository.findInProgressPersonIdsAfter(null, 100))
                .thenThrow(new PersistenceException("Banco indisponível")).thenReturn(List.of());

        assertThatThrownBy(useCase::execute).isInstanceOf(PersistenceException.class);
        useCase.execute();

        verify(repository, times(2)).findInProgressPersonIdsAfter(null, 100);
        verifyNoInteractions(expireWorkoutSessionUseCase);
    }
}
