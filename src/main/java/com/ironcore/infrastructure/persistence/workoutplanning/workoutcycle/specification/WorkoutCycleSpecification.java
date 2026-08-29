package com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.specification;

import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity.WorkoutCycleEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class WorkoutCycleSpecification {

    private WorkoutCycleSpecification() {
    }

    public static Specification<WorkoutCycleEntity> filter(
            Long personId,
            WorkoutStatus workoutStatus,
            Long trainingGoalId,
            LocalDate startDate,
            LocalDate endDate,
            String name
    ) {
        Specification<WorkoutCycleEntity> specification = belongsToPerson(personId);

        if (workoutStatus != null) {
            specification = specification.and(workoutStatus(workoutStatus));
        }

        if (trainingGoalId != null) {
            specification = specification.and(trainingGoalId(trainingGoalId));
        }

        if (startDate != null || endDate != null) {
            specification = specification.and(
                    period(startDate, endDate)
            );
        }

        if (name != null && !name.isBlank()) {
            specification = specification.and(nameContains(name));
        }

        return specification;
    }

    public static Specification<WorkoutCycleEntity> belongsToPerson(Long personId) {
        return (root, query, builder) ->
                builder.equal(root.get("person").get("id"), personId);
    }

    public static Specification<WorkoutCycleEntity> workoutStatus(WorkoutStatus workoutStatus) {
        return (root, query, builder) ->
                builder.equal(root.get("workoutStatus"), workoutStatus);
    }

    public static Specification<WorkoutCycleEntity> trainingGoalId(Long trainingGoalId) {
        return (root, query, builder) ->
                builder.equal(root.get("trainingGoal").get("id"), trainingGoalId);
    }

    public static Specification<WorkoutCycleEntity> period(
            LocalDate startDate,
            LocalDate endDate
    ) {
        return (root, query, builder) -> {
            if (startDate != null && endDate != null) {
                return builder.and(
                        builder.lessThanOrEqualTo(root.get("startDate"), endDate),
                        builder.or(
                                builder.isNull(root.get("endDate")),
                                builder.greaterThanOrEqualTo(root.get("endDate"), startDate)
                        )
                );
            }

            if (startDate != null) {
                return builder.or(
                        builder.isNull(root.get("endDate")),
                        builder.greaterThanOrEqualTo(root.get("endDate"), startDate)
                );
            }

            return builder.lessThanOrEqualTo(
                    root.get("startDate"),
                    endDate
            );
        };
    }

    public static Specification<WorkoutCycleEntity> nameContains(String name) {
        String normalizedName = escapeLike(name.trim().toLowerCase());

        return (root, query, builder) -> builder.like(
                builder.lower(root.get("name")),
                "%" + normalizedName + "%",
                '\\'
        );
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
