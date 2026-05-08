package com.ironcore.domain.user.valueobject;

import com.ironcore.domain.user.exception.InvalidUserException;

public record UserId(Long value) {

    public UserId {
        if (value == null || value <= 0) {
            throw new InvalidUserException("Id do usuário deve ser positivo");
        }
    }
}
