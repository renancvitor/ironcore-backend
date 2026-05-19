package com.ironcore.interfaces.rest.auth.dto;

import java.time.LocalDateTime;

public record LoginResponse(
        String accessToken,
        String tokenType,
        LocalDateTime expiresAt,
        Long userId,
        String email,
        String name,
        Boolean mustChangePassword
) {
}
