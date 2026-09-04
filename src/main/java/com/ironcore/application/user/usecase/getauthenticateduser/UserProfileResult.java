package com.ironcore.application.user.usecase.getauthenticateduser;

import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;

public record UserProfileResult(
        UserId userId,
        Email email,
        String nickname,
        Boolean mustChangePassword
) {
}
