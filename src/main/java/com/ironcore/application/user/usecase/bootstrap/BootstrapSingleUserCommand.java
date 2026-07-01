package com.ironcore.application.user.usecase.bootstrap;

import com.ironcore.domain.user.valueobject.Email;

import java.time.LocalDateTime;

public record BootstrapSingleUserCommand(
        String name,
        Email email,
        String rawPassword,
        Sex sex,
        LocalDateTime createdAt
) {
}
