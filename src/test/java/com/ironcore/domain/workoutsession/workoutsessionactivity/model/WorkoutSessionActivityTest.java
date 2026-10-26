package com.ironcore.domain.workoutsession.workoutsessionactivity.model;

import com.ironcore.domain.workoutsession.workoutsessionactivity.exception.InvalidWorkoutSessionActivityException;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.*;
import static org.assertj.core.api.Assertions.*;

class WorkoutSessionActivityTest {
    @Test
    void shouldValidateIdentifiers() {
        for (Long value : Arrays.asList(null, 0L, -1L)) {
            assertThatThrownBy(() -> new WorkoutSessionActivityId(value))
                    .isInstanceOf(InvalidWorkoutSessionActivityException.class);
        }
    }

    @Test
    void shouldReturnNewActivityWithoutMutatingSessionThroughExposedChild() {
        var session = session();
        var original = session.getActivities().getFirst();
        var changed = original.updateExecution(20, null, "Correção", START.plusSeconds(1))
                .recordSet(1, null, null, null, "Relato", START.plusSeconds(2));
        assertThat(changed.getActualDurationMinutes()).isEqualTo(20);
        assertThat(changed.getSets()).hasSize(1);
        assertThat(original.getActualDurationMinutes()).isNull();
        assertThat(original.getSets()).isEmpty();
        assertThat(session.getActivities().getFirst()).isSameAs(original);
        assertThat(session.getUpdatedAt()).isNull();
        assertThat(changed.getSnapshot()).isSameAs(original.getSnapshot());
    }
}
