package com.ironcore.interfaces.rest.exercise.catalog.dto;

public record MuscleSubgroupItemResponse(
        Long id,
        String code,
        Long muscleGroupId,
        String name
) {
}
