package com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutSessionActivitySnapshotTest {

    @ParameterizedTest
    @CsvSource({"45.5,45.50,3.1,3.10", "45.500,45.50,3.000,3.00", "0.0,0.00,0.00,0"})
    void shouldCompareEquivalentDecimalsAndKeepConsistentHashCode(
            BigDecimal firstLoad, BigDecimal secondLoad, BigDecimal firstDistance, BigDecimal secondDistance
    ) {
        WorkoutSessionActivitySnapshot first = snapshot(firstLoad, firstDistance);
        WorkoutSessionActivitySnapshot second = snapshot(secondLoad, secondDistance);
        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void shouldPreserveNumericDifferencesAndNullMeaning() {
        WorkoutSessionActivitySnapshot original = snapshot(new BigDecimal("45.50"), new BigDecimal("3.00"));
        assertThat(original).isNotEqualTo(snapshot(new BigDecimal("45.51"), new BigDecimal("3.00")));
        assertThat(original).isNotEqualTo(snapshot(new BigDecimal("45.50"), new BigDecimal("3.01")));
        assertThat(snapshot(null, null)).isNotEqualTo(snapshot(BigDecimal.ZERO, null));
        assertThat(snapshot(null, null)).isNotEqualTo(snapshot(null, BigDecimal.ZERO));
    }

    private WorkoutSessionActivitySnapshot snapshot(BigDecimal load, BigDecimal distance) {
        return new WorkoutSessionActivitySnapshot("Exercício", "STRENGTH", 1, 4, 8, 12,
                load, "Cada lado", 30, distance, "Moderada", 60, "Prescrição");
    }
}
