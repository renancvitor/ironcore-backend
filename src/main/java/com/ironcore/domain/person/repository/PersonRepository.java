package com.ironcore.domain.person.repository;

import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.valueobject.PersonId;

import java.util.Optional;

public interface PersonRepository {
    Person save(Person person);

    Optional<Person> findById(PersonId id);

    Optional<Person> findByName(String name);

    boolean existsById(PersonId id);

    boolean existsAny();
}
