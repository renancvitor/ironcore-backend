package com.ironcore.interfaces.rest.user.mapper;

import com.ironcore.application.user.usecase.ChangePasswordCommand;
import com.ironcore.application.user.usecase.InitialChangePasswordResult;
import com.ironcore.domain.user.valueobject.RawPassword;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.user.dto.ChangePasswordRequest;
import com.ironcore.interfaces.rest.user.dto.InitialChangePasswordResponse;

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

    public static InitialChangePasswordResponse toInitialChangePasswordResponse(
            InitialChangePasswordResult result
    ) {
        return new InitialChangePasswordResponse(
                result.accessToken(),
                result.tokenType(),
                result.expiresAt()
        );
    }
}
