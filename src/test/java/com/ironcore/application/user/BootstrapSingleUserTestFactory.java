package com.ironcore.application.user;

import com.ironcore.application.user.usecase.BootstrapSingleUserCommand;
import com.ironcore.domain.user.enums.SexType;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.Sex;

import java.time.LocalDateTime;

public final class BootstrapSingleUserTestFactory {

    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 5, 14, 10, 0);

    private BootstrapSingleUserTestFactory() {
    }

    public static BootstrapSingleUserCommand command() {
        return new BootstrapSingleUserCommand(
                "Renan",
                new Email("renan@example.com"),
                "StrongPass@2026",
                new Sex(SexType.MALE),
                CREATED_AT
        );
    }
}
