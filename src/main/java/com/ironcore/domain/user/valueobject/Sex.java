package com.ironcore.domain.user.valueobject;

import com.ironcore.domain.user.model.SexType;

import java.util.Objects;

public record Sex(SexType type) {

    public Sex {
        Objects.requireNonNull(type, "Sexo não pode ser nulo");
    }
}
