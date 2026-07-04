package com.ironcore.interfaces.rest.user.mapper;

import com.ironcore.application.user.usecase.getauthenticateduser.UserProfileResult;
import com.ironcore.application.user.usecase.update.ChangeNicknameCommand;
import com.ironcore.application.user.usecase.update.ChangeNicknameResult;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.user.dto.ChangeNicknameRequest;
import com.ironcore.interfaces.rest.user.dto.ChangeNicknameResponse;
import com.ironcore.interfaces.rest.user.dto.UserResponse;

public final class UserRestMapper {

    private UserRestMapper() {
    }

    public static UserResponse toResponse(UserProfileResult result) {
        return new UserResponse(
                result.userId().value(),
                result.email().value(),
                result.nickname()
        );
    }

    public static ChangeNicknameCommand toChangeNicknameCommand(
            AuthenticatedUser authenticatedUser,
            ChangeNicknameRequest request
    ) {
        return new ChangeNicknameCommand(
                authenticatedUser.userId(),
                request.nickname()
        );
    }

    public static ChangeNicknameResponse toResponse(ChangeNicknameResult result) {
        return new ChangeNicknameResponse(
                result.nickname()
        );
    }
}
