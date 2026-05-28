package com.ironcore.interfaces.rest.user.mapper;

import com.ironcore.application.user.usecase.ChangePasswordCommand;
import com.ironcore.application.user.usecase.InitialChangePasswordCommand;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.RawPassword;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.user.dto.ChangePasswordRequest;
import com.ironcore.interfaces.rest.user.dto.InitialChangePasswordRequest;

public final class UserChangePasswordMapper {

    private UserChangePasswordMapper() {
    }

    public static ChangePasswordCommand toChangePasswordCommand(
            AuthenticatedUser authenticatedUser,
            ChangePasswordRequest request
    ) {
        return new ChangePasswordCommand(
                authenticatedUser.userId(),
                new RawPassword(request.currentPassword()),
                new RawPassword(request.newPassword()),
                new RawPassword(request.confirmNewPassword())
        );
    }

    public static InitialChangePasswordCommand toInitialChangePasswordCommand(
            InitialChangePasswordRequest request) {
        return new InitialChangePasswordCommand(
                new Email(request.email()),
                new RawPassword(request.currentPassword()),
                new RawPassword(request.newPassword()),
                new RawPassword(request.confirmPassword())
        );
    }
}
