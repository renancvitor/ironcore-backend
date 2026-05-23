package com.ironcore.infrastructure.security.jwt;

import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;

public record JwtAccessTokenClaims(
        UserId userId,
        Email email,
        Boolean mustChangePassword
) {
}
