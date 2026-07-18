package com.ironcore.infrastructure.persistence.activitytype.repository;

import com.ironcore.infrastructure.persistence.activitytype.entity.ActivityTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivityTypeJpaRepository extends JpaRepository<ActivityTypeEntity, Long> {

    Optional<ActivityTypeEntity> findByCode(String code);
}
