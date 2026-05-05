package com.ironcore.domain.user.valueobject;

import com.ironcore.domain.user.model.SexType;

import java.util.Objects;

public record Sex(SexType type) {

    public Sex {
        if (type == null) {
            throw new IllegalArgumentException("Sexo não pode ser nulo");
        }
    }
}
