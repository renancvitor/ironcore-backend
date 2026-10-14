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
            String name, List<Long> activityTypeIds, List<Long> equipmentTypeIds,
            List<Long> muscleGroupIds, List<Long> muscleSubgroupIds, List<TargetRoleType> targetRoles
    ) {
        Specification<ExerciseEntity> specification = active(true);
        if (name != null && !name.isBlank()) specification = specification.and(nameContains(name));
        if (hasValues(activityTypeIds)) {
            specification = specification.and((root, query, builder) -> root.get("activityType").get("id").in(activityTypeIds));
        }
        if (hasValues(equipmentTypeIds)) {
            specification = specification.and((root, query, builder) -> root.get("equipmentType").get("id").in(equipmentTypeIds));
        }
        if (hasValues(muscleGroupIds) || hasValues(muscleSubgroupIds) || hasValues(targetRoles)) {
            specification = specification.and(hasMuscleTarget(muscleGroupIds, muscleSubgroupIds, targetRoles));
        }
        return specification;
    }

    private static boolean hasValues(List<?> values) {
        return values != null && !values.isEmpty();
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

    public static Specification<ExerciseEntity> hasMuscleTarget(
            List<Long> muscleGroupIds,
            List<Long> muscleSubgroupIds,
            List<TargetRoleType> targetRoles
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

            if (hasValues(muscleGroupIds)) {
                predicates.add(target.get("muscleSubgroup").get("muscleGroup").get("id").in(muscleGroupIds));
            }
            if (hasValues(muscleSubgroupIds)) {
                predicates.add(target.get("muscleSubgroup").get("id").in(muscleSubgroupIds));
            }
            if (hasValues(targetRoles)) {
                predicates.add(target.get("targetRole").in(targetRoles));
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
