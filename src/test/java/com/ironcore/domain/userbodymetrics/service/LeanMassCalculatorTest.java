package com.ironcore.domain.userbodymetrics.service;

import static com.ironcore.domain.userbodymetrics.UserBodyMetricsTestFactory.fatMass;
import static com.ironcore.domain.userbodymetrics.UserBodyMetricsTestFactory.heightInCm;
import static com.ironcore.domain.userbodymetrics.UserBodyMetricsTestFactory.validUser;
import static com.ironcore.domain.userbodymetrics.UserBodyMetricsTestFactory.weightInKg;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.data.Offset.offset;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ironcore.domain.userbodymetrics.exception.InvalidBodyMetricException;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.valueobject.LeanMassKg;

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
            UserBodyMetrics metrics = new UserBodyMetrics(
                    null,
                    validUser(),
                    LocalDateTime.now(),
                    weightInKg(82.0),
                    heightInCm(180.0),
                    null,
                    null,
                    null,
                    fatMass(18.04),
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
                    .isThrownBy(() -> calculator.calculate((UserBodyMetrics) null))
                    .withMessage("Medidas corporais não podem ser nulo");
        }

        @Test
        void shouldRequireWeight() {
            assertThatExceptionOfType(InvalidBodyMetricException.class)
                    .isThrownBy(() -> calculator.calculate(null, fatMass(18.04)))
                    .withMessage("Peso não pode ser nulo");
        }

        @Test
        void shouldRequireFatMass() {
            assertThatExceptionOfType(InvalidBodyMetricException.class)
                    .isThrownBy(() -> calculator.calculate(weightInKg(82.0), null))
                    .withMessage("Massa gorda não pode ser nulo");
        }
    }
}
