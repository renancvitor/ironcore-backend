package com.ironcore.domain.bodymetrics.service;

import static com.ironcore.domain.bodymetrics.BodyMetricsTestFactory.heightInCm;
import static com.ironcore.domain.bodymetrics.BodyMetricsTestFactory.userId;
import static com.ironcore.domain.bodymetrics.BodyMetricsTestFactory.weightInKg;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.data.Offset.offset;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ironcore.domain.bodymetrics.exception.InvalidBodyMetricException;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.valueobject.BMI;

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
            BodyMetrics metrics = new BodyMetrics(
                    null,
                    userId(),
                    LocalDateTime.now(),
                    weightInKg(72.0),
                    heightInCm(180.0),
                    null,
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
                    .isThrownBy(() -> calculator.calculate((BodyMetrics) null))
                    .withMessage("Medidas corporais são obrigatórias.");
        }

        @Test
        void shouldRequireHeight() {
            assertThatExceptionOfType(InvalidBodyMetricException.class)
                    .isThrownBy(() -> calculator.calculate(null, weightInKg(81.0)))
                    .withMessage("Altura é obrigatória.");
        }

        @Test
        void shouldRequireWeight() {
            assertThatExceptionOfType(InvalidBodyMetricException.class)
                    .isThrownBy(() -> calculator.calculate(heightInCm(180.0), null))
                    .withMessage("Peso é obrigatório.");
        }
    }
}
