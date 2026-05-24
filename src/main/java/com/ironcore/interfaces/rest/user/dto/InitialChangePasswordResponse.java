package com.ironcore.interfaces.rest.user.dto;

import java.time.LocalDateTime;

public record InitialChangePasswordResponse(
        String accessToken,
        String tokenType,
        LocalDateTime expiresAt
) {
}
