package com.ironcore.domain.userbodymetrics.service;

import static com.ironcore.domain.userbodymetrics.UserBodyMetricsTestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.data.Offset.offset;

import com.ironcore.domain.user.model.User;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.valueobject.FatMassKg;
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
            UserBodyMetrics metrics = new UserBodyMetrics(
                    null,
                    new User(),
                    LocalDateTime.now(),
                    weightInKg(82.0),
                    heightInCm(180.0),
                    null,
                    null,
                    bodyFatPercentage(22.0),
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
            assertThatNullPointerException()
                    .isThrownBy(() -> calculator.calculate((UserBodyMetrics) null))
                    .withMessage("Medidas corporais não podem ser nulo");
        }

        @Test
        void shouldRequireWeight() {
            assertThatNullPointerException()
                    .isThrownBy(() -> calculator.calculate(null, bodyFatPercentage(22.0)))
                    .withMessage("Peso não pode ser nulo");
        }

        @Test
        void shouldRequireBodyFatPercentage() {
            assertThatNullPointerException()
                    .isThrownBy(() -> calculator.calculate(weightInKg(82.0), null))
                    .withMessage("Percentual de gordura não pode ser nulo");
        }
    }
}
