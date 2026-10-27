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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkoutSessionSetTest {
    @ParameterizedTest
    @CsvSource({
            "1,,TOTAL,10,", "1,10.00,,10,",
            "0,10.00,TOTAL,10,", "-1,10.00,TOTAL,10,",
            "1,-1.00,TOTAL,10,", "1,10.00,TOTAL,0,",
            "1,10.001,TOTAL,10,", "1,1000000.00,TOTAL,10,",
            "1,,,,", "1,,TOTAL,,texto", "1,,,0,", "1,,,-1,"
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
    void shouldUpdateSetInPlaceAndPreserveIdentityAndCreation() {
        WorkoutSessionSet set = WorkoutSessionSet.restore(new WorkoutSessionSetId(50L), 1,
                new BigDecimal("45.00"), ActualLoadMode.PER_SIDE, 10, null, START, null);
        set.updateSet(new BigDecimal("50.00"), ActualLoadMode.PER_SIDE, 8, "Correção", START.plusSeconds(1));
        assertThat(set.getId()).isEqualTo(new WorkoutSessionSetId(50L));
        assertThat(set.getActualLoadKg()).isEqualByComparingTo("50");
        assertThat(set.getCreatedAt()).isEqualTo(START);
        assertThat(set.getUpdatedAt()).isEqualTo(START.plusSeconds(1));
        assertThatThrownBy(() -> set.updateSet(new BigDecimal("-1"), ActualLoadMode.PER_SIDE,
                8, "Inválido", START.plusSeconds(2))).isInstanceOf(InvalidWorkoutSessionSetException.class);
        assertThat(set.getActualLoadKg()).isEqualByComparingTo("50");
        assertThat(set.getNotes()).isEqualTo("Correção");
    }

    @Test
    void shouldAcceptTrailingZerosWithoutLosingPrecision() {
        WorkoutSessionSet set = WorkoutSessionSet.register(1, new BigDecimal("10.000"),
                ActualLoadMode.TOTAL, 10, null, START);
        assertThat(set.getActualLoadKg()).isEqualByComparingTo("10");
    }

    @Test
    void shouldRecordRepsWithoutPresumingExternalLoad() {
        WorkoutSessionSet set = WorkoutSessionSet.register(1, null, null, 12, null, START);
        assertThat(set.getActualReps()).isEqualTo(12);
        assertThat(set.getActualLoadKg()).isNull();
        assertThat(set.getActualLoadMode()).isNull();
        assertThat(set.getNormalizedLoadKg()).isNull();
    }

    @Test
    void shouldRecordLoadWithoutPresumingRepsAndNormalizeOnlyExplicitLoad() {
        WorkoutSessionSet set = WorkoutSessionSet.register(1, new BigDecimal("999999.99"),
                ActualLoadMode.PER_SIDE, null, null, START);
        assertThat(set.getActualReps()).isNull();
        assertThat(set.getActualLoadKg()).isEqualByComparingTo("999999.99");
        assertThat(set.getNormalizedLoadKg()).isEqualByComparingTo("1999999.98");
        set.changeLoadMode(ActualLoadMode.TOTAL, START.plusSeconds(1));
        assertThat(set.getActualLoadKg()).isEqualByComparingTo("999999.99");
        assertThat(set.getNormalizedLoadKg()).isEqualByComparingTo("999999.99");
        set.updateSet(BigDecimal.ZERO, ActualLoadMode.TOTAL, null, null, START.plusSeconds(2));
        assertThat(set.getNormalizedLoadKg()).isEqualByComparingTo("0");
    }

    @Test
    void shouldClearMetricsIndependentlyAndRejectClearingLastUsefulValue() {
        WorkoutSessionSet set = WorkoutSessionSet.register(1, new BigDecimal("20.25"),
                ActualLoadMode.PER_SIDE, 8, null, START);
        set.updateSet(null, null, 8, null, START.plusSeconds(1));
        assertThat(set.getActualReps()).isEqualTo(8);
        assertThat(set.getNormalizedLoadKg()).isNull();
        assertThatThrownBy(() -> set.updateSet(null, null, null, " ", START.plusSeconds(2)))
                .isInstanceOf(InvalidWorkoutSessionSetException.class);
        assertThat(set.getActualReps()).isEqualTo(8);
        set.updateSet(null, null, null, "Relato", START.plusSeconds(2));
        assertThat(set.getActualReps()).isNull();
    }
}
