package com.ironcore.application.user;

import com.ironcore.application.user.usecase.changepassword.ChangePasswordCommand;
import com.ironcore.domain.user.valueobject.RawPassword;
import com.ironcore.domain.user.valueobject.UserId;

public final class ChangePasswordTestFactory {

    private ChangePasswordTestFactory() {
    }

    public static ChangePasswordCommand command() {
        return new ChangePasswordCommand(
                new UserId(1L),
                new RawPassword("StrongOldPassword"),
                new RawPassword("StrongNewPassword"),
                new RawPassword("StrongNewPassword")
        );
    }

    public static ChangePasswordCommand commandWithDifferentPasswordConfirmation() {
        return new ChangePasswordCommand(
                new UserId(1L),
                new RawPassword("StrongOldPassword"),
                new RawPassword("StrongNewPassword"),
                new RawPassword("StrongAnyPassword")
        );
    }
}
