package com.ironcore.application.auth.port;

import java.time.Instant;

import com.ironcore.domain.auth.valueobject.AuthSessionId;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;

public record AccessTokenSubject(
        UserId userId,
        Email email,
        Boolean mustChangePassword,
        AuthSessionId sessionId,
        Instant sessionExpiresAt
) {
    public AccessTokenSubject(UserId userId, Email email, Boolean mustChangePassword, AuthSessionId sessionId) {
        this(userId, email, mustChangePassword, sessionId, null);
    }
}
