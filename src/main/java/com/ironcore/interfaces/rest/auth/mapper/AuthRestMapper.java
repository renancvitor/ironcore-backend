package com.ironcore.interfaces.rest.auth.mapper;

import com.ironcore.application.auth.usecase.LoginCommand;
import com.ironcore.application.auth.usecase.LoginResult;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.interfaces.rest.auth.dto.LoginRequest;
import com.ironcore.interfaces.rest.auth.dto.LoginResponse;

public final class AuthRestMapper {

    private AuthRestMapper() {
    }

    public static LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(
                new Email(request.email()),
                request.password()
        );
    }

    public static LoginResponse toResponse(LoginResult result) {
        return new LoginResponse(
                result.accessToken(),
                result.tokenType(),
                result.expiresAt(),
                result.userId().value(),
                result.email().value(),
                result.nickname(),
                result.mustChangePassword()
        );
    }
}
