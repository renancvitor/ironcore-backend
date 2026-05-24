package com.ironcore.interfaces.rest.user.mapper;

import com.ironcore.application.user.usecase.ChangePasswordCommand;
import com.ironcore.domain.user.valueobject.RawPassword;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.user.dto.ChangePasswordRequest;

public final class ChangePasswordMapper {

    private ChangePasswordMapper() {
    }

    public static ChangePasswordCommand toCommand(
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
}
