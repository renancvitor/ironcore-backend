package com.ironcore.infrastructure.persistence.person;

import com.ironcore.domain.person.enums.SexType;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class PersonEntityTestFactory {

    public static final LocalDate BIRTH_DATE = LocalDate.of(1994, 4, 9);
    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 5, 10, 10, 0);
    public static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 5, 10, 11, 0);

    private PersonEntityTestFactory() {
    }

    public static PersonEntity personEntity() {
        return personEntity(1L);
    }

    private static PersonEntity personEntity(Long id) {
        return new PersonEntity(
                id,
                "Renan",
                SexType.MALE,
                BIRTH_DATE,
                CREATED_AT,
                null
        );
    }
}
