package com.ironcore.interfaces.rest.user.mapper;

import com.ironcore.application.user.usecase.getauthenticateduser.UserProfileResult;
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
}
