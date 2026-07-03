package com.ironcore.application.person;

import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.domain.person.model.Person;

import java.time.LocalDate;

public record PersonAuditData(
        Long id,
        String name,
        String sex,
        LocalDate birthDate
) implements LoggableData {

    public static PersonAuditData from(Person person) {
        return new PersonAuditData(
                person.getId().value(),
                person.getName(),
                person.getSex().type().name(),
                person.getBirthDate().value()
        );
    }
}
