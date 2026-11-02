package com.ironcore.application.auth.service;

import com.ironcore.application.auth.port.AccessTokenGenerator;
import com.ironcore.application.auth.port.AccessTokenSubject;
import com.ironcore.application.auth.port.RefreshTokenGenerator;
import com.ironcore.application.auth.port.SessionCredentials;
import com.ironcore.domain.auth.model.AuthSession;
import com.ironcore.domain.auth.repository.AuthSessionRepository;
import com.ironcore.domain.auth.valueobject.AuthSessionId;
import com.ironcore.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthSessionIssuer {

    private static final Duration SESSION_DURATION = Duration.ofDays(7);

    private final AuthSessionRepository authSessionRepository;
    private final AccessTokenGenerator accessTokenGenerator;
    private final Clock clock;
    private final RefreshTokenGenerator refreshTokenGenerator;

    public SessionCredentials issue(User user) {
        Instant now = clock.instant();
        String refreshToken = refreshTokenGenerator.generate();

        AuthSession session = authSessionRepository.save(
                AuthSession.open(
                        new AuthSessionId(UUID.randomUUID()),
                        user.getId(),
                        now,
                        now.plus(SESSION_DURATION),
                        refreshTokenGenerator.hash(refreshToken)
                )
        );

        return new SessionCredentials(accessTokenGenerator.generate(
                new AccessTokenSubject(
                        user.getId(),
                        user.getEmail(),
                        user.mustChangePassword(),
                        session.getId(),
                        session.getExpiresAt()
                )
        ), refreshToken, session.getExpiresAt());
    }
}
