package com.ironcore.domain.person.valueobject;

import com.ironcore.domain.person.exception.InvalidPersonException;

import java.time.LocalDate;
import java.time.Period;

public record BirthDate(LocalDate value) {

    public BirthDate{
        if (value == null) {
            throw new InvalidPersonException("Data de nascimento é obrigatório.");
        }

        if (value.isAfter(LocalDate.now())) {
            throw new InvalidPersonException("Data de nascimento não pode estar no futuro.");
        }

        if (value.isBefore(LocalDate.now().minusYears(120))) {
            throw new InvalidPersonException("Data de nascimento inválida.");
        }
    }

    public int ageAt(LocalDate referenceDate) {
        return Period.between(value, referenceDate).getYears();
    }
}
