package com.ironcore.domain.bodymetrics.service;

import static com.ironcore.domain.bodymetrics.BodyMetricsTestFactory.bodyFatPercentage;
import static com.ironcore.domain.bodymetrics.BodyMetricsTestFactory.heightInCm;
import static com.ironcore.domain.user.UserTestFactory.personId;
import static com.ironcore.domain.bodymetrics.BodyMetricsTestFactory.weightInKg;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.data.Offset.offset;

import com.ironcore.domain.bodymetrics.exception.InvalidBodyMetricException;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.valueobject.FatMassKg;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class FatMassCalculatorTest {

    private final FatMassCalculator calculator = new FatMassCalculator();

    @Nested
    class SuccessfulCalculations {

        @Test
        void shouldCalculateFatMassFromWeightAndBodyFatPercentage() {
            FatMassKg result = calculator.calculate(weightInKg(70.0), bodyFatPercentage(15.0));

            assertThat(result.value()).isCloseTo(10.5, offset(0.01));
        }

        @Test
        void shouldCalculateFatMassFromBodyMetrics() {
            BodyMetrics metrics = BodyMetrics.restore(
                    null,
                    personId(1L),
                    LocalDateTime.now(),
                    weightInKg(82.0),
                    heightInCm(180.0),
                    null,
                    null,
                    bodyFatPercentage(22.0),
                    null,
                    null,
                    null,
                    null);

            FatMassKg result = calculator.calculate(metrics);

            assertThat(result.value()).isCloseTo(18.04, offset(0.01));
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
                    .isThrownBy(() -> calculator.calculate(null, bodyFatPercentage(22.0)))
                    .withMessage("Peso é obrigatório.");
        }

        @Test
        void shouldRequireBodyFatPercentage() {
            assertThatExceptionOfType(InvalidBodyMetricException.class)
                    .isThrownBy(() -> calculator.calculate(weightInKg(82.0), null))
                    .withMessage("Percentual de gordura é obrigatório.");
        }
    }
}
