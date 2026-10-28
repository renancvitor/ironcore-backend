package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.application.workoutsession.workoutsession.analytics.ExerciseCardioExecutionResult;
import com.ironcore.application.workoutsession.workoutsession.analytics.ExerciseStrengthExecutionResult;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseAnalyticsQueryAdapterTest {

    private static final PersonId PERSON_ID = new PersonId(1L);
    private static final ExerciseId EXERCISE_ID = new ExerciseId(42L);
    private static final LocalDate DATE = LocalDate.of(2026, 10, 26);
    private static final Instant START = Instant.parse("2026-10-26T12:00:00Z");

    @Mock
    private ExerciseAnalyticsJpaRepository repository;

    @InjectMocks
    private ExerciseAnalyticsQueryAdapter adapter;

    @Test
    void shouldMapStrengthProjectionPreservingNullsAndDecimals() {
        var projection = new ExerciseStrengthExecutionProjection(
                10L, DATE, START, "Histórico", 20L, new BigDecimal("20.10"), ActualLoadMode.PER_SIDE, null
        );
        var withoutSets = new ExerciseStrengthExecutionProjection(
                10L, DATE, START, "Histórico", null, null, null, null
        );
        when(repository.findStrengthExecution(1L, 42L, DATE, DATE)).thenReturn(List.of(projection, withoutSets));

        assertThat(adapter.findStrengthExecution(PERSON_ID, EXERCISE_ID, DATE, DATE))
                .containsExactly(
                        new ExerciseStrengthExecutionResult(
                                new WorkoutSessionId(10L), DATE, START, "Histórico", new WorkoutSessionSetId(20L),
                                new BigDecimal("20.10"), ActualLoadMode.PER_SIDE, null
                        ),
                        new ExerciseStrengthExecutionResult(
                                new WorkoutSessionId(10L), DATE, START, "Histórico", null, null, null, null
                        )
                );
    }

    @Test
    void shouldMapCardioProjectionWithoutPairingOrFillingMissingMeasurements() {
        var projection = new ExerciseCardioExecutionProjection(10L, DATE, START, "Histórico", null, BigDecimal.ZERO);
        when(repository.findCardioExecution(1L, 42L, DATE, DATE)).thenReturn(List.of(projection));

        assertThat(adapter.findCardioExecution(PERSON_ID, EXERCISE_ID, DATE, DATE))
                .containsExactly(new ExerciseCardioExecutionResult(new WorkoutSessionId(10L), DATE, START, "Histórico", null, BigDecimal.ZERO));
    }

    @Test
    void shouldTranslateStrengthPersistenceFailure() {
        when(repository.findStrengthExecution(1L, 42L, DATE, DATE)).thenThrow(new IllegalStateException("offline"));

        assertThatThrownBy(() -> adapter.findStrengthExecution(PERSON_ID, EXERCISE_ID, DATE, DATE))
                .isInstanceOf(PersistenceException.class)
                .hasRootCauseMessage("offline");
    }

    @Test
    void shouldTranslateCardioPersistenceFailure() {
        when(repository.findCardioExecution(1L, 42L, DATE, DATE)).thenThrow(new IllegalStateException("offline"));

        assertThatThrownBy(() -> adapter.findCardioExecution(PERSON_ID, EXERCISE_ID, DATE, DATE))
                .isInstanceOf(PersistenceException.class)
                .hasRootCauseMessage("offline");
    }

    @Test
    void shouldDistinguishStrengthMappingFailureFromPersistenceFailure() {
        when(repository.findStrengthExecution(1L, 42L, DATE, DATE)).thenReturn(Collections.singletonList(null));

        assertThatThrownBy(() -> adapter.findStrengthExecution(PERSON_ID, EXERCISE_ID, DATE, DATE))
                .isInstanceOf(DataMappingException.class);
    }

    @Test
    void shouldDistinguishCardioMappingFailureFromPersistenceFailure() {
        when(repository.findCardioExecution(1L, 42L, DATE, DATE)).thenReturn(Collections.singletonList(null));

        assertThatThrownBy(() -> adapter.findCardioExecution(PERSON_ID, EXERCISE_ID, DATE, DATE))
                .isInstanceOf(DataMappingException.class);
    }
}
