package com.ironcore.domain.auth.valueobject;

import com.ironcore.domain.auth.exception.InvalidAuthSessionException;

import java.util.UUID;

public record AuthSessionId(UUID value) {

    public AuthSessionId {
        if (value == null) {
            throw new InvalidAuthSessionException("Identificador da sessão não pode ser nulo.");
        }
    }
}
