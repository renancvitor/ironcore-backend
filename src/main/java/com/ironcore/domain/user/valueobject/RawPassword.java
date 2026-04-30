package com.ironcore.domain.user.valueobject;

public record RawPassword(String value) {

    public RawPassword {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("A senha não pode ser nula ou vazia");
        }

        if (value.length() < 8) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 8 caracteres");
        }
    }

}
