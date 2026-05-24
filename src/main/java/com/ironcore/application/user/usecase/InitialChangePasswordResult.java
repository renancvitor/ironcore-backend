package com.ironcore.application.user.usecase;

import java.time.LocalDateTime;

public record InitialChangePasswordResult(
        String accessToken,
        String tokenType,
        LocalDateTime expiredAt
) {
}
