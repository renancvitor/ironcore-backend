package com.ironcore.infrastructure.persistence.user.repository;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.user.mapper.UserMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final PersonJpaRepository personJpaRepository;
    private final EntityManager entityManager;

    @Override
    public Optional<User> findByIdForUpdate(UserId id) {
        Optional<UserEntity> entity;
        try {
            entity = userJpaRepository.findLockedById(id.value());
            entity.ifPresent(entityManager::refresh);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao bloquear usuário por id.", exception);
        }

        try {
            return entity.map(UserMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter usuário bloqueado para domínio.", exception);
        }
    }

    @Override
    public Optional<User> findByEmailForUpdate(Email email) {
        Optional<UserEntity> entity;
        try {
            entity = userJpaRepository.findLockedByEmail(email.value());
            entity.ifPresent(entityManager::refresh);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao bloquear usuário por email.", exception);
        }

        try {
            return entity.map(UserMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter usuário bloqueado para domínio.", exception);
        }
    }

    @Override
    public User save(User user) {
        PersonEntity personReference = personJpaRepository.getReferenceById(user.getPersonId().value());
        UserEntity entity;
        try {
            entity = Objects.requireNonNull(
                    UserMapper.toEntity(user,  personReference),
                    "UserMapper retornou entidade nula."
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter user de domínio para entidade.", exception);
        }

        UserEntity saveEntity;
        try {
            saveEntity = Objects.requireNonNull(
                    userJpaRepository.save(entity),
                    "UserMapper retornou entidade nula após persistência."
            );
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao persistir user.", exception);
        }

        try {
            return UserMapper.toDomain(saveEntity);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter user persistido para domínio.", exception);
        }
    }

    @Override
    public Optional<User> findById(UserId id) {
        Optional<UserEntity> entity;
        try {
            Long userId = Objects.requireNonNull(id.value(), "Id do usuário não pode ser nulo.");
            entity = userJpaRepository.findById(userId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar user por id.", exception);
        }

        try {
            return entity.map(UserMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter user encontrado por id para domínio.", exception);
        }
    }

    @Override
    public Optional<User> findByPersonId(PersonId id) {
        Optional<UserEntity> entity;
        try {
            Long personId = Objects.requireNonNull(id.value(), "Id da pessoa não pode ser nulo.");
            entity = userJpaRepository.findByPerson_Id(personId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar user por id da pessoa.", exception);
        }

        try {
            return entity.map(UserMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter user encontrado por id da pessoa para domínio.", exception);
        }
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        Optional<UserEntity> entity;
        try {
            entity = userJpaRepository.findByEmail(email.value());
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar user por email.", exception);
        }

        try {
            return entity.map(UserMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter user encontrado por email para domínio.", exception);
        }
    }

    @Override
    public boolean existsById(UserId id) {
        try {
            Long userId = Objects.requireNonNull(id.value(), "Id do usuário não pode ser nulo.");
            return userJpaRepository.existsById(userId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao verificar existência de user por id.", exception);
        }
    }

    @Override
    public boolean existsByEmail(Email email) {
        try {
            return userJpaRepository.existsByEmail(email.value());
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao verificar existência de user por email.", exception);
        }
    }

    @Override
    public boolean existsAny() {
        try {
            return userJpaRepository.count() > 0;
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao verificar existência de qualquer user.", exception);
        }
    }
}
