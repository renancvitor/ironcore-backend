package com.ironcore.domain.workoutsession.workoutsessionactivity.model;

import com.ironcore.domain.workoutsession.workoutsessionactivity.exception.InvalidWorkoutSessionActivityException;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Arrays;

import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.START;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.session;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkoutSessionActivityTest {
    @Test
    void shouldValidateIdentifiers() {
        for (Long value : Arrays.asList(null, 0L, -1L)) {
            assertThatThrownBy(() -> new WorkoutSessionActivityId(value))
                    .isInstanceOf(InvalidWorkoutSessionActivityException.class);
        }
    }

    @Test
    void shouldUpdateActivityInPlaceAndPreserveSnapshot() {
        var session = session();
        var activity = session.getActivities().getFirst();
        var snapshot = activity.getSnapshot();
        activity.updateExecution(20, null, "Correção", START.plusSeconds(1));
        activity.addSet(1, null, null, null, "Relato", START.plusSeconds(2));
        assertThat(activity.getActualDurationMinutes()).isEqualTo(20);
        assertThat(activity.getSets()).hasSize(1);
        assertThat(session.getActivities().getFirst()).isSameAs(activity);
        assertThat(activity.getSnapshot()).isSameAs(snapshot);
    }

    @Test
    void shouldRejectInvalidUpdateBeforeChangingAnyField() {
        var activity = session().getActivities().getFirst();
        assertThatThrownBy(() -> activity.updateExecution(20, new BigDecimal("-1.00"),
                "Inválido", START.plusSeconds(1))).isInstanceOf(InvalidWorkoutSessionActivityException.class);
        assertThat(activity.getActualDurationMinutes()).isNull();
        assertThat(activity.getNotes()).isNull();
        assertThat(activity.getUpdatedAt()).isNull();
    }

    @Test
    void shouldRecordDurationDistanceAndNotesIndependently() {
        var activity = session().getActivities().getFirst();
        activity.updateExecution(null, new BigDecimal("0.00"), null, START.plusSeconds(1));
        assertThat(activity.getActualDurationMinutes()).isNull();
        assertThat(activity.getActualDistanceKm()).isEqualByComparingTo("0");
        activity.updateExecution(30, null, null, START.plusSeconds(2));
        assertThat(activity.getActualDurationMinutes()).isEqualTo(30);
        assertThat(activity.getActualDistanceKm()).isNull();
        activity.updateExecution(null, null, "Relato", START.plusSeconds(3));
        assertThat(activity.getActualDurationMinutes()).isNull();
        assertThat(activity.getNotes()).isEqualTo("Relato");
        assertThat(activity.getSets()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"0,", "-1,", ",-0.01", ",0.001", ",1000000.00"})
    void shouldRejectInvalidIndependentMetrics(Integer duration, BigDecimal distance) {
        var activity = session().getActivities().getFirst();
        assertThatThrownBy(() -> activity.updateExecution(duration, distance, "Inválido", START.plusSeconds(1)))
                .isInstanceOf(InvalidWorkoutSessionActivityException.class);
        assertThat(activity.getActualDurationMinutes()).isNull();
        assertThat(activity.getActualDistanceKm()).isNull();
        assertThat(activity.getNotes()).isNull();
    }
}
