package com.ironcore.domain.workoutsession.workoutsessionset.model;

import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.domain.workoutsession.workoutsessionset.exception.InvalidWorkoutSessionSetException;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Arrays;

import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.START;
import static org.assertj.core.api.Assertions.*;

class WorkoutSessionSetTest {
    @ParameterizedTest
    @CsvSource({
            "1,,TOTAL,10,", "1,10.00,,10,", "1,10.00,TOTAL,,",
            "0,10.00,TOTAL,10,", "-1,10.00,TOTAL,10,",
            "1,-1.00,TOTAL,10,", "1,10.00,TOTAL,0,",
            "1,10.001,TOTAL,10,", "1,1000000.00,TOTAL,10,",
            "1,,,,", "1,,TOTAL,,texto"
    })
    void shouldRejectInvalidOrEmptySets(Integer number, BigDecimal load, ActualLoadMode mode, Integer reps, String notes) {
        assertThatThrownBy(() -> WorkoutSessionSet.register(number, load, mode, reps, notes, START))
                .isInstanceOf(InvalidWorkoutSessionSetException.class);
    }

    @Test
    void shouldValidateIdentifiers() {
        for (Long value : Arrays.asList(null, 0L, -1L)) {
            assertThatThrownBy(() -> new WorkoutSessionSetId(value)).isInstanceOf(InvalidWorkoutSessionSetException.class);
        }
    }

    @Test
    void shouldReturnUpdatedSetWithoutMutatingOriginal() {
        WorkoutSessionSet original = WorkoutSessionSet.register(1, new BigDecimal("45.00"),
                ActualLoadMode.PER_SIDE, 10, null, START);
        WorkoutSessionSet updated = original.update(new BigDecimal("50.00"), ActualLoadMode.PER_SIDE,
                8, "Correção", START.plusSeconds(1));
        assertThat(original.getActualLoadKg()).isEqualByComparingTo("45");
        assertThat(original.getUpdatedAt()).isNull();
        assertThat(updated.getActualLoadKg()).isEqualByComparingTo("50");
        assertThat(updated.getCreatedAt()).isEqualTo(START);
    }
}
