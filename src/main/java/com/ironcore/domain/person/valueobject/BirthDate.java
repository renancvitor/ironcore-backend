package com.ironcore.domain.person.valueobject;

import com.ironcore.domain.person.exception.InvalidPersonException;

import java.time.LocalDate;
import java.time.Period;

public record BirthDate(LocalDate value) {

    public static BirthDate from(LocalDate value, LocalDate referenceDate) {
        BirthDate birthDate = new BirthDate(value);

        if (referenceDate == null) {
            throw new InvalidPersonException("Data de referência é obrigatória.");
        }

        if (value.isAfter(referenceDate)) {
            throw new InvalidPersonException("Data de nascimento não pode estar no futuro.");
        }

        if (value.isBefore(referenceDate.minusYears(120))) {
            throw new InvalidPersonException("Data de nascimento inválida.");
        }

        return birthDate;
    }

    public int ageAt(LocalDate referenceDate) {
        if (referenceDate == null) {
            throw new InvalidPersonException("Data de referência é obrigatória.");
        }

        return Period.between(value, referenceDate).getYears();
    }
}
