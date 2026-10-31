package com.ironcore.interfaces.rest.auth.dto;

import java.time.LocalDateTime;

public record LoginResponse(
        LocalDateTime expiresAt,
        Long userId,
        String email,
        String nickname,
        Boolean mustChangePassword
) {
}
