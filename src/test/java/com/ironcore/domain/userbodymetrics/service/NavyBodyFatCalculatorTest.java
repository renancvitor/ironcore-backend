package com.ironcore.domain.userbodymetrics.service;

import static com.ironcore.domain.userbodymetrics.UserBodyMetricsTestFactory.heightInCm;
import static com.ironcore.domain.userbodymetrics.UserBodyMetricsTestFactory.navyCircumferences;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.userbodymetrics.exception.InvalidBodyMetricException;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferences;
import com.ironcore.domain.userbodymetrics.valueobject.BodyFatPercentage;
import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;

class NavyBodyFatCalculatorTest {

    private final NavyBodyFatCalculator calculator = new NavyBodyFatCalculator();

    @Nested
    class SuccessfulCalculations {

        @Test
        void shouldCalculateMaleBodyFatPercentage() {
            BodyFatPercentage result = calculator.calculate(
                    SexType.MALE,
                    heightInCm(178.0),
                    navyCircumferences(39.0, 88.0));

            assertThat(result.value()).isCloseTo(18.0, withinPercentagePoint());
        }

        @Test
        void shouldCalculateFemaleBodyFatPercentage() {
            BodyFatPercentage result = calculator.calculate(
                    SexType.FEMALE,
                    heightInCm(165.0),
                    navyCircumferences(33.0, 70.0, 98.0));

            assertThat(result.value()).isCloseTo(26.2, withinPercentagePoint());
        }
    }

    @Nested
    class ValidationErrors {

        @Test
        void shouldRequireHipCircumferenceForFemaleCalculation() {
            BodyHeightCm height = heightInCm(165.0);
            BodyCircumferences circumferencesWithoutHip = navyCircumferences(33.0, 70.0);

            assertThatThrownBy(() -> calculator.calculate(SexType.FEMALE, height, circumferencesWithoutHip))
                    .isInstanceOf(InvalidBodyMetricException.class)
                    .hasMessage("Circunferência do quadril é obrigatória.");
        }
    }

    private org.assertj.core.data.Offset<Double> withinPercentagePoint() {
        return offset(0.1);
    }
}
