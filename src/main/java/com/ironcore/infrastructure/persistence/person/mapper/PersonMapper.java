package com.ironcore.infrastructure.persistence.person.mapper;

import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.person.valueobject.Sex;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;

public class PersonMapper {

    public static PersonEntity toEntity(Person person) {
        try {
            return new PersonEntity(
                    person.getId() == null ? null : person.getId().value(),
                    person.getName(),
                    person.getSex().type(),
                    person.getBirthDate().value(),
                    person.getCreatedAt(),
                    person.getUpdatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter user de domínio para entidade.", exception);
        }
    }

    public static Person toDomain(PersonEntity entity) {
        try {
            return new Person(
                    new PersonId(entity.getId()),
                    entity.getName(),
                    new Sex(entity.getSex()),
                    new BirthDate(entity.getBirthDate()),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter user de entidade para domínio.", exception);
        }
    }
}
