package com.ironcore.application.auth.service;

import com.ironcore.application.auth.port.AccessTokenGenerator;
import com.ironcore.application.auth.port.AccessTokenSubject;
import com.ironcore.application.auth.port.GeneratedAccessToken;
import com.ironcore.application.auth.port.RefreshTokenGenerator;
import com.ironcore.domain.auth.model.AuthSession;
import com.ironcore.domain.auth.repository.AuthSessionRepository;
import com.ironcore.domain.auth.valueobject.AuthSessionId;
import com.ironcore.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthSessionIssuer {

    private Duration sessionDuration = Duration.ofDays(7);

    @Value("${security.session.duration-days:7}")
    public void setSessionDurationDays(long days) {
        if (days < 1 || days > 7) {
            throw new IllegalArgumentException("security.session.duration-days deve estar entre 1 e 7.");
        }
        this.sessionDuration = Duration.ofDays(days);
    }

    private final AuthSessionRepository authSessionRepository;
    private final AccessTokenGenerator accessTokenGenerator;
    private final Clock clock;
    private final RefreshTokenGenerator refreshTokenGenerator;

    public GeneratedAccessToken issue(User user) {
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);

        AuthSession session = AuthSession.open(
                new AuthSessionId(UUID.randomUUID()),
                user.getId(),
                now,
                now.plus(sessionDuration)
        );
        return renew(user, session);
    }

    public GeneratedAccessToken renew(User user, AuthSession session) {
        String refresh = refreshTokenGenerator.generate(session.getId());
        session.rotateRefreshCredential(refreshTokenGenerator.hash(refresh), clock.instant().truncatedTo(ChronoUnit.MICROS));
        authSessionRepository.save(session);
        GeneratedAccessToken access = accessTokenGenerator.generate(
                new AccessTokenSubject(
                        user.getId(),
                        user.getEmail(),
                        user.mustChangePassword(),
                        session.getId(),
                        session.getExpiresAt()
                )
        );
        return new GeneratedAccessToken(access.value(), access.tokenType(), access.expiresAt(), refresh, session.getExpiresAt());
    }
}
