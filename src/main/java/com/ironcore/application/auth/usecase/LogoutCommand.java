package com.ironcore.application.auth.usecase;

import com.ironcore.domain.auth.valueobject.AuthSessionId;
import com.ironcore.domain.user.valueobject.UserId;

public record LogoutCommand(
        UserId userId,
        AuthSessionId sessionId
) {
}
