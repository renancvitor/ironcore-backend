package com.ironcore.infrastructure.persistence.auth.repository;

import com.ironcore.infrastructure.persistence.auth.entity.AuthenticationAttemptEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import java.time.Instant;
import java.util.Optional;

public interface AuthenticationAttemptJpaRepository extends JpaRepository<AuthenticationAttemptEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AuthenticationAttemptEntity> findLockedById(String id);
    long deleteByWindowStartedAtBefore(Instant cutoff);
}
