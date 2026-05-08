package com.ironcore.domain.user.valueobject;

import com.ironcore.domain.user.exception.InvalidPasswordException;

public record PasswordHash(String value) {

    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new InvalidPasswordException("O hash da senha não pode ser nulo ou vazio");
        }
    }

}
