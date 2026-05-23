package com.ironcore.infrastructure.security.auth;

import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;

public record AuthenticatedUser(
        UserId userId,
        Email email,
        Boolean mustChangePassword
) {
}
