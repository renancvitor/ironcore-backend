package com.ironcore.domain.person.valueobject;

import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.exception.InvalidPersonException;

public record Sex(SexType type) {

    public Sex {
        if (type == null) {
            throw new InvalidPersonException("Sexo não pode ser nulo.");
        }
    }
}
