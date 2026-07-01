package com.ironcore.infrastructure.persistence.person.mapper;

import com.ironcore.domain.person.model.Person;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.infrastructure.persistence.person.PersonEntityTestFactory.personEntity;
import static org.assertj.core.api.Assertions.assertThat;

class PersonMapperTest {

    @Nested
    class ToEntity {

        @Test
        void shouldMapSecurityAndStatusFields() {
            Person person = restoredPerson();

            PersonEntity entity = PersonMapper.toEntity(person);

            assertThat(entity.getName()).isEqualTo(person.getName());
            assertThat(entity.getBirthDate()).isEqualTo(person.getBirthDate().value());
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldRestoreSecurityAndStatusFields() {
            PersonEntity entity = personEntity();

            Person person = PersonMapper.toDomain(entity);

            assertThat(person.getName()).isEqualTo(entity.getName());
            assertThat(person.getBirthDate().value()).isEqualTo(entity.getBirthDate());
        }
    }
}
