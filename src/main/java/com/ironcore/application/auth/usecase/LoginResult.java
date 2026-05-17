package com.ironcore.application.auth.usecase;

import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;

import java.time.LocalDateTime;

public record LoginResult(
        String accessToken,
        String tokenType,
        LocalDateTime expiresAt,
        UserId userId,
        Email email,
        String name,
        Boolean mustChangePassword
) {
}
