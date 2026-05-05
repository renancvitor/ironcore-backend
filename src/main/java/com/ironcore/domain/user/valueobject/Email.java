package com.ironcore.domain.user.valueobject;

import com.ironcore.domain.user.exception.InvalidEmailException;

import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new InvalidEmailException("Email não pode ser nulo ou vazio");
        }

        value = value.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException("O e-mail informado possui um formato inválido.");
        }
    }

}
