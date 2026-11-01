package com.ironcore.infrastructure.persistence.user.repository;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;

import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserEntity> findLockedById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserEntity> findLockedByEmail(String email);

    Optional<UserEntity> findByPerson_Id(Long personId);

    boolean existsByEmail(String email);
}
