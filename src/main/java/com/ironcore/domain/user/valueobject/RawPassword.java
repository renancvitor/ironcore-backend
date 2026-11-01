package com.ironcore.domain.user.valueobject;

import com.ironcore.domain.user.exception.InvalidPasswordException;

public record RawPassword(String value) {

    public RawPassword {
        if (value == null || value.isBlank()) {
            throw new InvalidPasswordException("A senha não pode ser nula ou vazia");
        }

        if (value.length() < 8) {
            throw new InvalidPasswordException("A senha deve ter pelo menos 8 caracteres");
        }
    }

}
