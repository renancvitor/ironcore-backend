package com.ironcore.infrastructure.persistence.logging.error.repository;

import com.ironcore.infrastructure.persistence.logging.error.entity.ErrorLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorLogJpaRepository extends JpaRepository<ErrorLogEntity, Long> {
}
