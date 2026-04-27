package com.ironcore.domain.userbodymetrics.valueobject;

public record BodyCircumferences(
        BodyCircumferenceCm neck,
        BodyCircumferenceCm chest,
        BodyCircumferenceCm waist,
        BodyCircumferenceCm hip,
        BodyCircumferenceCm arm,
        BodyCircumferenceCm thigh,
        BodyCircumferenceCm calf
) {
}
