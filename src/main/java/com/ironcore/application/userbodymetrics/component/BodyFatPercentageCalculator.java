package com.ironcore.application.userbodymetrics.component;

import com.ironcore.domain.user.enums.SexType;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.userbodymetrics.service.NavyBodyFatCalculator;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferences;
import com.ironcore.domain.userbodymetrics.valueobject.BodyFatPercentage;
import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BodyFatPercentageCalculator {

    private final NavyBodyFatCalculator navyBodyFatCalculator;

    public BodyFatPercentage calculate(
            User user,
            BodyHeightCm height,
            BodyCircumferences circumferences
    ) {
        if (circumferences == null) {
            return null;
        }

        if (user.getSex().type() == SexType.MALE
                && circumferences.neck() != null
                && circumferences.waist() != null) {
            return navyBodyFatCalculator.calculate(user.getSex().type(), height, circumferences);
        }

        if (user.getSex().type() == SexType.FEMALE
                && circumferences.neck() != null
                && circumferences.waist() != null
                && circumferences.hip() != null) {
            return navyBodyFatCalculator.calculate(user.getSex().type(), height, circumferences);
        }

        return null;
    }
}
