package com.ironcore.application.auth;

import com.ironcore.application.auth.port.GeneratedAccessToken;
import com.ironcore.application.auth.usecase.LoginCommand;
import com.ironcore.domain.user.valueobject.Email;

import java.time.LocalDateTime;

public final class LoginTestFactory {

    public static final LocalDateTime EXPIRES_AT = LocalDateTime.of(2026, 5, 17, 12, 0);

    private LoginTestFactory() {
    }

    public static LoginCommand command() {
        return new LoginCommand(
                new Email("renan@example.com"),
                "StrongPass123@"
        );
    }

    public static GeneratedAccessToken generatedAccessToken() {
        return new GeneratedAccessToken(
                "access-token",
                "Bearer",
                EXPIRES_AT
        );
    }
}
