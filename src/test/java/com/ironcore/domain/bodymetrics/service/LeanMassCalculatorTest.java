package com.ironcore.domain.bodymetrics.service;

import static com.ironcore.domain.bodymetrics.BodyMetricsTestFactory.fatMass;
import static com.ironcore.domain.bodymetrics.BodyMetricsTestFactory.heightInCm;
import static com.ironcore.domain.user.UserTestFactory.personId;
import static com.ironcore.domain.bodymetrics.BodyMetricsTestFactory.weightInKg;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.data.Offset.offset;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ironcore.domain.bodymetrics.exception.InvalidBodyMetricException;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.valueobject.LeanMassKg;

class LeanMassCalculatorTest {

    private final LeanMassCalculator calculator = new LeanMassCalculator();

    @Nested
    class SuccessfulCalculations {

        @Test
        void shouldCalculateLeanMassFromWeightAndFatMass() {
            LeanMassKg result = calculator.calculate(weightInKg(82.0), fatMass(18.04));

            assertThat(result.value()).isCloseTo(63.96, offset(0.01));
        }

        @Test
        void shouldCalculateLeanMassFromBodyMetrics() {
            BodyMetrics metrics = BodyMetrics.restore(
                    null,
                    personId(1L),
                    LocalDateTime.now(),
                    weightInKg(82.0),
                    heightInCm(180.0),
                    null,
                    null,
                    null,
                    fatMass(18.04),
                    null,
                    null,
                    null);

            LeanMassKg result = calculator.calculate(metrics);

            assertThat(result.value()).isCloseTo(63.96, offset(0.01));
        }

    }

    @Nested
    class ValidationErrors {

        @Test
        void shouldRequireBodyMetrics() {
            assertThatExceptionOfType(InvalidBodyMetricException.class)
                    .isThrownBy(() -> calculator.calculate((BodyMetrics) null))
                    .withMessage("Medidas corporais são obrigatórias.");
        }

        @Test
        void shouldRequireWeight() {
            assertThatExceptionOfType(InvalidBodyMetricException.class)
                    .isThrownBy(() -> calculator.calculate(null, fatMass(18.04)))
                    .withMessage("Peso é obrigatório.");
        }

        @Test
        void shouldRequireFatMass() {
            assertThatExceptionOfType(InvalidBodyMetricException.class)
                    .isThrownBy(() -> calculator.calculate(weightInKg(82.0), null))
                    .withMessage("Massa gorda é obrigatória.");
        }
    }
}
