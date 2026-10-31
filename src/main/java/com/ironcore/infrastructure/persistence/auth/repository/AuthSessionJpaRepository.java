package com.ironcore.infrastructure.persistence.auth.repository;

import com.ironcore.infrastructure.persistence.auth.entity.AuthSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthSessionJpaRepository extends JpaRepository<AuthSessionEntity, UUID> {

    Optional<AuthSessionEntity> findByIdAndUser_Id(UUID id, Long userId);

    List<AuthSessionEntity> findAllByUser_IdAndRevokedAtIsNull(Long userId);
}
