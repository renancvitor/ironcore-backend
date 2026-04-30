package com.ironcore.domain.user.valueobject;

public record PasswordHash(String value) {

    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("O hash da senha não pode ser nulo ou vazio");
        }
    }

}
