package com.ironcore.application.user.usecase.changepassword;

import com.ironcore.domain.user.valueobject.RawPassword;
import com.ironcore.domain.user.valueobject.UserId;

public record ChangePasswordCommand(
        UserId userId,
        RawPassword currentPassword,
        RawPassword newPassword,
        RawPassword confirmPassword
) {
}
