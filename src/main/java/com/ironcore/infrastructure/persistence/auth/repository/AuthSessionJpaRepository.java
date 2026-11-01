package com.ironcore.infrastructure.persistence.auth.repository;

import org.springframework.data.jpa.repository.Query;

import com.ironcore.infrastructure.persistence.auth.entity.AuthSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthSessionJpaRepository extends JpaRepository<AuthSessionEntity, UUID> {

    @Query("select s.user.id from AuthSessionEntity s where s.id = :id")
    Optional<Long> findUserIdById(UUID id);

    Optional<AuthSessionEntity> findByIdAndUser_Id(UUID id, Long userId);

    List<AuthSessionEntity> findAllByUser_IdAndRevokedAtIsNull(Long userId);
}
