package com.ironcore.application.bodymetrics.component;

import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.bodymetrics.service.NavyBodyFatCalculator;
import com.ironcore.domain.bodymetrics.valueobject.BodyCircumferences;
import com.ironcore.domain.bodymetrics.valueobject.BodyFatPercentage;
import com.ironcore.domain.bodymetrics.valueobject.BodyHeightCm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BodyFatPercentageCalculator {

    private final NavyBodyFatCalculator navyBodyFatCalculator;

    public BodyFatPercentage calculate(
            Person person,
            BodyHeightCm height,
            BodyCircumferences circumferences
    ) {
        if (circumferences == null) {
            return null;
        }

        if (person.getSex().type() == SexType.MALE
                && circumferences.neck() != null
                && circumferences.waist() != null) {
            return navyBodyFatCalculator.calculate(person.getSex().type(), height, circumferences);
        }

        if (person.getSex().type() == SexType.FEMALE
                && circumferences.neck() != null
                && circumferences.waist() != null
                && circumferences.hip() != null) {
            return navyBodyFatCalculator.calculate(person.getSex().type(), height, circumferences);
        }

        return null;
    }
}
