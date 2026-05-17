package com.ironcore.application.auth.usecase;

import com.ironcore.domain.user.valueobject.Email;

public record LoginCommand(
        Email email,
        String rawPassword
) {
}
