package com.ironcore.infrastructure.security.cookie;

import com.ironcore.application.auth.port.GeneratedAccessToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class AccessTokenCookieFactory {

    private final Clock clock;
    private final String sameSite;

    public AccessTokenCookieFactory(
            Clock clock,
            @Value("${security.cookie.same-site:None}") String sameSite
    ) {
        this.clock = clock;
        this.sameSite = sameSite;
    }

    public String create(GeneratedAccessToken token) {
        Duration maxAge = Duration.between(
                LocalDateTime.now(clock),
                token.expiresAt()
        );

        return ResponseCookie.from("access_token", token.value())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAge.isNegative() ? Duration.ZERO : maxAge)
                .sameSite(sameSite)
                .build()
                .toString();
    }

    public String clear() {
        return ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite(sameSite)
                .build()
                .toString();
    }
}
