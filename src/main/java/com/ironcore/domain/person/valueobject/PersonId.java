package com.ironcore.domain.person.valueobject;

import com.ironcore.domain.person.exception.InvalidPersonException;

public record PersonId(Long value) {

    public PersonId {
        if (value == null || value <= 0) {
            throw new InvalidPersonException("Id da pessoa deve ser positivo.");
        }
    }
}
