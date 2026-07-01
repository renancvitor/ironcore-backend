package com.ironcore.application.user.usecase.bootstrap;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.user.valueobject.Email;

import java.time.LocalDateTime;

public record BootstrapSingleUserCommand(
        String nickname,
        PersonId personId,
        Email email,
        String rawPassword,
        LocalDateTime createdAt
) {
}
