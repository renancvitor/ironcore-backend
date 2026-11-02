package com.ironcore.application.auth.usecase;

import com.ironcore.application.auth.port.RefreshTokenGenerator;
import com.ironcore.domain.auth.repository.AuthSessionRepository;
import com.ironcore.domain.auth.valueobject.AuthSessionId;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    private final AuthSessionRepository authSessionRepository;
    private final Clock clock;
    private final UserRepository userRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;

    @Transactional
    public void execute(LogoutCommand command) {
        if (command.userId() == null || command.sessionId() == null) {
            revokeByRefreshToken(command.refreshToken());
            return;
        }
        revokeSession(command.sessionId(), command.userId());
    }

    private void revokeSession(AuthSessionId sessionId, UserId userId) {
        if (userRepository.findByIdForUpdate(userId).isEmpty()) {
            return;
        }

        authSessionRepository
                .findByIdAndUserId(sessionId, userId)
                .ifPresent(session -> {
                    session.revoke(clock.instant());
                    authSessionRepository.save(session);
                });
    }

    private void revokeByRefreshToken(String refreshToken) {
        String hash;
        try {
            hash = refreshTokenGenerator.hash(refreshToken);
        } catch (IllegalArgumentException exception) {
            return;
        }

        authSessionRepository.findReferenceByRefreshTokenHash(hash)
                .ifPresent(reference -> revokeSession(reference.sessionId(), reference.userId()));
    }
}
