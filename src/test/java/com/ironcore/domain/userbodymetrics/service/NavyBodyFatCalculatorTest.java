package com.ironcore.domain.userbodymetrics.service;

import com.ironcore.domain.user.model.Sex;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferenceCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferences;
import com.ironcore.domain.userbodymetrics.valueobject.BodyFatPercentage;
import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NavyBodyFatCalculatorTest {

    private final NavyBodyFatCalculator calculator = new NavyBodyFatCalculator();

    @Test
    void shouldCalculateMaleBodyFatPercentage() {
        BodyFatPercentage result = calculateMaleBodyFat();

        assertThat(result.value()).isCloseTo(18.0, withinPercentagePoint());
    }

    private BodyFatPercentage calculateMaleBodyFat() {
        return calculator.calculate(
                Sex.MALE,
                new BodyHeightCm(178.0),
                new BodyCircumferences(
                        new BodyCircumferenceCm(39.0),
                        null,
                        new BodyCircumferenceCm(88.0),
                        null,
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void shouldCalculateFemaleBodyFatPercentage() {
        BodyFatPercentage result = calculateFemaleBodyFat();

        assertThat(result.value()).isCloseTo(26.2, withinPercentagePoint());
    }

    private BodyFatPercentage calculateFemaleBodyFat() {
        return calculator.calculate(
                Sex.FEMALE,
                new BodyHeightCm(165.0),
                new BodyCircumferences(
                        new BodyCircumferenceCm(33.0),
                        null,
                        new BodyCircumferenceCm(70.0),
                        new BodyCircumferenceCm(98.0),
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void shouldRequireHipCircumferenceForFemaleCalculation() {
        assertThatThrownBy(this::calculateFemaleBodyFatWithoutHip)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hip circumference is required");
    }

    private BodyFatPercentage calculateFemaleBodyFatWithoutHip() {
        return calculator.calculate(
                Sex.FEMALE,
                new BodyHeightCm(165.0),
                new BodyCircumferences(
                        new BodyCircumferenceCm(33.0),
                        null,
                        new BodyCircumferenceCm(70.0),
                        null,
                        null,
                        null,
                        null
                )
        );
    }

    private org.assertj.core.data.Offset<Double> withinPercentagePoint() {
        return org.assertj.core.data.Offset.offset(0.1);
    }
}
