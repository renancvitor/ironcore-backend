package com.ironcore.infrastructure.persistence.userbodymetrics.specification;

import com.ironcore.infrastructure.persistence.userbodymetrics.entity.UserBodyMetricsEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class UserBodyMetricsSpecifications {

    public static Specification<UserBodyMetricsEntity> measuredAtBetween(
            LocalDateTime from,
            LocalDateTime to
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("measuredAt"), from, to);
    }
}
