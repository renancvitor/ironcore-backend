package com.ironcore.application.auth;

import com.ironcore.application.auth.port.GeneratedAccessToken;
import com.ironcore.application.auth.usecase.LoginCommand;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.valueobject.Email;

import java.time.LocalDateTime;

import static com.ironcore.domain.user.UserTestFactory.restoredUser;

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

    public static User activeUser() {
        return restoredUser(false, true);
    }

    public static User inactiveUser() {
        return restoredUser(false, false);
    }

    public static GeneratedAccessToken generatedAccessToken() {
        return new GeneratedAccessToken(
                "access-token",
                "Bearer",
                EXPIRES_AT
        );
    }
}
