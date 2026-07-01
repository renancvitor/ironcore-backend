package com.ironcore.infrastructure.persistence.person.repository;

import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.person.mapper.PersonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PersonRepositoryAdapter implements PersonRepository {

    private final PersonJpaRepository personJpaRepository;

    @Override
    public Person save(Person person) {
        PersonEntity entity;
        try {
            entity = Objects.requireNonNull(
                    PersonMapper.toEntity(person),
                    "PersonMapper retornou entidade nula."
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter person de domínio para entidade.", exception);
        }

        PersonEntity saveEntity;
        try {
            saveEntity = Objects.requireNonNull(
                    personJpaRepository.save(entity),
                    "PersonMapper retornou entidade nula após persistência."
            );
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao persistir person.", exception);
        }

        try {
            return PersonMapper.toDomain(saveEntity);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter person persistido para domínio.", exception);
        }
    }

    @Override
    public Optional<Person> findById(PersonId id) {
        Optional<PersonEntity> entity;
        try {
            Long personId = Objects.requireNonNull(id.value(),"Id da pessoa não pode ser nulo.");
            entity = personJpaRepository.findById(personId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar person por id.", exception);
        }

        try {
            return entity.map(PersonMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter person encontrado por id para domínio.", exception);
        }
    }

    @Override
    public Optional<Person> findByName(String name) {
        Optional<PersonEntity> entity;
        try {
            entity = personJpaRepository.findByName(name);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar person por nome.", exception);
        }

        try {
            return entity.map(PersonMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter person encontrado por nome para domínio.", exception);
        }
    }

    @Override
    public boolean existsById(PersonId id) {
        try {
            Long personId = Objects.requireNonNull(id.value(), "Id da pessoa não pode ser nulo.");
            return personJpaRepository.existsById(personId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao verificar existência de person por id.", exception);
        }
    }

    @Override
    public boolean existsAny() {
        try {
            return personJpaRepository.count() > 0;
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao verificar existência de qualquer person.", exception);
        }
    }
}
