package com.ironcore.application.person;

import com.ironcore.application.person.usecase.bootstrap.BootstrapPersonCommand;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.Sex;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class BootstrapPersonTestFactory {

    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 5, 14, 10, 0);
    public static final BirthDate BIRTH_DATE = new BirthDate(LocalDate.of(1994, 4, 9));

    private BootstrapPersonTestFactory() {
    }

    public static BootstrapPersonCommand command() {
        return new BootstrapPersonCommand(
                "Renan C Vitor",
                new Sex(SexType.MALE),
                BIRTH_DATE,
                CREATED_AT
        );
    }

    public static Person existingPerson() {
        BootstrapPersonCommand command = command();

        return Person.register(
                command.name(),
                command.sex(),
                command.birthDate(),
                command.createdAt());
    }
}
