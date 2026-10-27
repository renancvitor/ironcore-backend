package com.ironcore.domain.workoutsession.workoutsessionactivity.model;

import com.ironcore.domain.workoutsession.workoutsessionactivity.exception.InvalidWorkoutSessionActivityException;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;
import org.junit.jupiter.api.Test;

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
}
