package com.ironcore.domain.userbodymetrics.service;

import static com.ironcore.domain.userbodymetrics.UserBodyMetricsTestFactory.heightInCm;
import static com.ironcore.domain.userbodymetrics.UserBodyMetricsTestFactory.weightInKg;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.data.Offset.offset;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ironcore.domain.user.model.User;
import com.ironcore.domain.userbodymetrics.exception.InvalidBodyMetricException;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.valueobject.BMI;

class BMICalculatorTest {

    private final BMICalculator calculator = new BMICalculator();

    @Nested
    class SuccessfulCalculations {

        @Test
        void shouldCalculateBmiFromHeightAndWeight() {
            BMI result = calculator.calculate(heightInCm(180.0), weightInKg(81.0));

            assertThat(result.value()).isCloseTo(25.0, offset(0.01));
        }

        @Test
        void shouldCalculateBmiFromBodyMetrics() {
            UserBodyMetrics metrics = new UserBodyMetrics(
                    null,
                    new User(),
                    LocalDateTime.now(),
                    weightInKg(72.0),
                    heightInCm(180.0),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);

            BMI result = calculator.calculate(metrics);

            assertThat(result.value()).isCloseTo(22.22, offset(0.01));
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
        void shouldRequireHeight() {
            assertThatExceptionOfType(InvalidBodyMetricException.class)
                    .isThrownBy(() -> calculator.calculate(null, weightInKg(81.0)))
                    .withMessage("Altura não pode ser nulo");
        }

        @Test
        void shouldRequireWeight() {
            assertThatExceptionOfType(InvalidBodyMetricException.class)
                    .isThrownBy(() -> calculator.calculate(heightInCm(180.0), null))
                    .withMessage("Peso não pode ser nulo");
        }
    }
}
