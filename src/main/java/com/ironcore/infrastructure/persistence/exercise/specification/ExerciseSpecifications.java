package com.ironcore.infrastructure.persistence.exercise.specification;

import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import com.ironcore.infrastructure.persistence.exercisemuscletarget.entity.ExerciseMuscleTargetEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ExerciseSpecifications {

    private ExerciseSpecifications() {
    }

    public static Specification<ExerciseEntity> filter(
            String name,
            Long activityTypeId,
            Long equipmentTypeId,
            Long muscleGroupId,
            Long muscleSubgroupId,
            TargetRoleType targetRole
    ) {
        Specification<ExerciseEntity> specification = active(true);

        if (name != null && !name.isBlank()) {
            specification = specification.and(nameContains(name));
        }

        if (activityTypeId != null) {
            specification = specification.and(activityTypeId(activityTypeId));
        }

        if (equipmentTypeId != null) {
            specification = specification.and(equipmentTypeId(equipmentTypeId));
        }

        if (muscleGroupId != null) {
            TargetRoleType groupTargetRole =
                    muscleSubgroupId == null ? targetRole : null;

            specification = specification.and(
                    hasMuscleTarget(
                            muscleGroupId,
                            null,
                            groupTargetRole
                    )
            );
        }

        if (muscleSubgroupId != null) {
            specification = specification.and(
                    hasMuscleTarget(
                            null,
                            muscleSubgroupId,
                            targetRole
                    )
            );
        }

        if (muscleGroupId == null && muscleSubgroupId == null && targetRole != null) {
            specification = specification.and(
                    hasMuscleTarget(
                            null,
                            null,
                            targetRole
                    )
            );
        }

        return specification;
    }

    public static Specification<ExerciseEntity> active(Boolean active) {
        return (root, query, builder) ->
                builder.equal(root.get("active"), active);
    }

    public static Specification<ExerciseEntity> nameContains(String name) {
        String normalizedName = escapeLike(name.trim().toLowerCase(Locale.ROOT));

        return (root, query, builder) -> builder.like(
                builder.lower(root.get("name")),
                "%" + normalizedName + "%",
                '\\'
        );
    }

    public static Specification<ExerciseEntity> activityTypeId(Long activityTypeId) {
        return (root, query, builder) ->
                builder.equal(root.get("activityType").get("id"), activityTypeId);
    }

    public static Specification<ExerciseEntity> equipmentTypeId(Long equipmentTypeId) {
        return (root, query, builder) ->
                builder.equal(root.get("equipmentType").get("id"), equipmentTypeId);
    }

    public static Specification<ExerciseEntity> hasMuscleTarget(
            Long muscleGroupId,
            Long muscleSubgroupId,
            TargetRoleType targetRole
    ) {
        return (root, query, builder) -> {
            if (query == null) {
                throw new IllegalStateException(
                        "CriteriaQuery não pode ser nula para criar a subquery."
                );
            }

            var subquery = query.subquery(Long.class);
            var target = subquery.from(ExerciseMuscleTargetEntity.class);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    builder.equal(target.get("exercise").get("id"), root.get("id"))
            );

            predicates.add(
                    builder.isTrue(target.get("active"))
            );

            if (muscleGroupId != null) {
                predicates.add(
                        builder.equal(
                                target.get("muscleSubgroup")
                                        .get("muscleGroup")
                                        .get("id"),
                                muscleGroupId
                        )
                );
            }

            if (muscleSubgroupId != null) {
                predicates.add(
                        builder.equal(
                                target.get("muscleSubgroup")
                                        .get("id"),
                                muscleSubgroupId
                        )
                );
            }

            if (targetRole != null) {
                predicates.add(
                        builder.equal(
                                target.get("targetRole"),
                                targetRole
                        )
                );
            }

            subquery.select(target.get("id"))
                    .where(predicates.toArray(Predicate[]::new));

            return builder.exists(subquery);
        };
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
