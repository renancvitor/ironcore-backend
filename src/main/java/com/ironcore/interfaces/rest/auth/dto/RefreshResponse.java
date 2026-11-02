package com.ironcore.interfaces.rest.auth.dto;

import java.time.LocalDateTime;

public record RefreshResponse(
        LocalDateTime expiresAt
) {
}
