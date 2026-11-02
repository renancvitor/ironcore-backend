package com.ironcore.application.auth.port;

import com.ironcore.domain.auth.valueobject.AuthSessionId;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;

import java.time.Instant;

public record AccessTokenSubject(
        UserId userId,
        Email email,
        Boolean mustChangePassword,
        AuthSessionId sessionId,
        Instant sessionExpiresAt
) {
}
