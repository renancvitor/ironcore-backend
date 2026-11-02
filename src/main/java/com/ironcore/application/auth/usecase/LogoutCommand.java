package com.ironcore.application.auth.usecase;

import com.ironcore.domain.auth.valueobject.AuthSessionId;
import com.ironcore.domain.user.valueobject.UserId;

public record LogoutCommand(
        UserId userId,
        AuthSessionId sessionId,
        String refreshToken
) {
    @Override
    public String toString() {
        return "LogoutCommand[userId=" + userId + ", sessionId=" + sessionId + ", refreshToken=[REDACTED]]";
    }
}
