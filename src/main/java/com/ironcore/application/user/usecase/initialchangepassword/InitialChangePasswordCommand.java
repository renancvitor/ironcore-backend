package com.ironcore.application.user.usecase.initialchangepassword;

import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.RawPassword;

public record InitialChangePasswordCommand(
        Email email,
        RawPassword currentPassword,
        RawPassword newPassword,
        RawPassword confirmPassword
) {
}
