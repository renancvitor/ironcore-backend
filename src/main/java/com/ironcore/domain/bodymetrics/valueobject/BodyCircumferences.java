package com.ironcore.domain.bodymetrics.valueobject;

public record BodyCircumferences(
        BodyCircumferenceCm neck,
        BodyCircumferenceCm chest,
        BodyCircumferenceCm shoulder,
        BodyCircumferenceCm arm,
        BodyCircumferenceCm forearm,
        BodyCircumferenceCm waist,
        BodyCircumferenceCm hip,
        BodyCircumferenceCm thigh,
        BodyCircumferenceCm calf
) {
}
