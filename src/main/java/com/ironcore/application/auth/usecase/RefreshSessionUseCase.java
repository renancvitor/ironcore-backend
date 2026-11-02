package com.ironcore.application.auth.usecase;

import com.ironcore.application.auth.port.AccessTokenGenerator;
import com.ironcore.application.auth.port.AccessTokenSubject;
import com.ironcore.application.auth.port.RefreshTokenGenerator;
import com.ironcore.application.auth.port.SessionCredentials;
import com.ironcore.application.exception.InvalidCredentialsException;
import com.ironcore.domain.auth.model.AuthSession;
import com.ironcore.domain.auth.model.AuthSessionReference;
import com.ironcore.domain.auth.repository.AuthSessionRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class RefreshSessionUseCase {

    private final AuthSessionRepository sessions;
    private final UserRepository users;
    private final RefreshTokenGenerator refreshTokens;
    private final AccessTokenGenerator accessTokens;
    private final Clock clock;

    @Transactional
    public SessionCredentials execute(String refreshToken) {
        String hash;
        try {
            hash = refreshTokens.hash(refreshToken);
        } catch (IllegalArgumentException exception) {
            throw invalidCredentials();
        }

        UserId userId = sessions.findReferenceByRefreshTokenHash(hash)
                .map(AuthSessionReference::userId).orElseThrow(this::invalidCredentials);
        User user = users.findByIdForUpdate(userId).orElseThrow(this::invalidCredentials);
        AuthSession session = sessions.findByRefreshTokenHash(hash).orElseThrow(this::invalidCredentials);
        if (!user.isActive() || user.mustChangePassword() || !session.isActiveAt(clock.instant())) {
            throw invalidCredentials();
        }

        String nextToken = refreshTokens.generate();
        session.rotateRefreshToken(refreshTokens.hash(nextToken), clock.instant());
        sessions.save(session);

        return new SessionCredentials(accessTokens.generate(new AccessTokenSubject(
                user.getId(), user.getEmail(), user.mustChangePassword(), session.getId(), session.getExpiresAt()
        )), nextToken, session.getExpiresAt());
    }

    private InvalidCredentialsException invalidCredentials() {
        return new InvalidCredentialsException("Credenciais inválidas.");
    }
}
