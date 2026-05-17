package com.ironcore.application.auth.port;

import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;

public record AccessTokenSubject(
        UserId userId,
        Email email,
        Boolean mustChangePassword
) {
}
