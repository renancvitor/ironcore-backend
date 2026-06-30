package com.ironcore.domain.person;

import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.person.valueobject.Sex;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class PersonTestFactory {

    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 5, 10, 10, 0);
    public static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 5, 10, 11, 0);
    public static final BirthDate BIRTH_DATE = new BirthDate(LocalDate.of(1994, 4, 9));

    private PersonTestFactory() {
    }

    public static Person personWithoutId() {
        return Person.register(
                "Renan",
                sex(SexType.MALE),
                BIRTH_DATE,
                CREATED_AT
        );
    }

    public static Person restoredPerson() {
        return Person.restore(
                new PersonId(1L),
                "Renan",
                sex(SexType.MALE),
                BIRTH_DATE,
                CREATED_AT,
                UPDATED_AT
        );
    }

    public static Sex sex(SexType type) {
        return new Sex(type);
    }
}
