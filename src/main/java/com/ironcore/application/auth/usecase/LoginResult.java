package com.ironcore.application.auth.usecase;

import com.ironcore.application.auth.port.SessionCredentials;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;

public record LoginResult(
        SessionCredentials credentials,
        UserId userId,
        Email email,
        String nickname,
        Boolean mustChangePassword
) {
}
