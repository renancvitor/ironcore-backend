package com.ironcore.infrastructure.persistence.person.repository;

import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonJpaRepository extends JpaRepository<PersonEntity, Long> {
    Optional<PersonEntity> findByName(String name);
}
