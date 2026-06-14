package com.ironcore.interfaces.rest.userbodymetrics.dto;

public record BodyCircumferencesResponse(
        Double neckCm,
        Double chestCm,
        Double shoulderCm,
        Double armCm,
        Double forearmCm,
        Double waistCm,
        Double hipCm,
        Double thighCm,
        Double calfCm
) {
}
