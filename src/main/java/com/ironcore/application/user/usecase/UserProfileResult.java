package com.ironcore.application.user.usecase;

import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;

public record UserProfileResult(
        UserId userId,
        Email email,
        String name
) {
}
