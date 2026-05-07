package com.ironcore.domain.user.valueobject;

import com.ironcore.domain.user.enums.SexType;
import com.ironcore.domain.user.exception.InvalidUserException;

public record Sex(SexType type) {

    public Sex {
        if (type == null) {
            throw new InvalidUserException("Sexo não pode ser nulo");
        }
    }
}
