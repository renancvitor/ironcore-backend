package com.ironcore.infrastructure.persistence.auth.repository;

import com.ironcore.infrastructure.persistence.auth.entity.AuthSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthSessionJpaRepository extends JpaRepository<AuthSessionEntity, UUID> {

    Optional<AuthSessionEntity> findByIdAndUser_Id(UUID id, Long userId);

    @Query("select s.id as sessionId, s.user.id as userId from AuthSessionEntity s where s.refreshTokenHash = :hash")
    Optional<SessionReference> findReferenceByRefreshTokenHash(String hash);

    Optional<AuthSessionEntity> findByRefreshTokenHash(String hash);

    List<AuthSessionEntity> findAllByUser_IdAndRevokedAtIsNull(Long userId);

    interface SessionReference {
        UUID getSessionId();

        Long getUserId();
    }
}
