package com.ironcore.domain.auth;

import com.ironcore.domain.auth.valueobject.AuthSessionId;

import java.util.UUID;

public final class AuthSessionTestFactory {

    private AuthSessionTestFactory() {
    }

    public static AuthSessionId sessionId() {
        return new AuthSessionId(UUID.fromString("38bd6914-32e4-4bc8-afd2-deba635aa64c"));
    }
}
