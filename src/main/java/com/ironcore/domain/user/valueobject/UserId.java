package com.ironcore.domain.user.valueobject;

public record UserId(Long value) {

    public UserId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Id do usuário deve ser positivo");
        }
    }
}
