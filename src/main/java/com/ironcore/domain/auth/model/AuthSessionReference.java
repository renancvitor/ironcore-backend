package com.ironcore.domain.auth.model;

import com.ironcore.domain.auth.valueobject.AuthSessionId;
import com.ironcore.domain.user.valueobject.UserId;

public record AuthSessionReference(
        AuthSessionId sessionId,
        UserId userId
) {
}
