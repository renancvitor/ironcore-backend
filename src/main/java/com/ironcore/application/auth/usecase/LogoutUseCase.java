package com.ironcore.application.auth.usecase;

import org.springframework.transaction.annotation.Transactional;

import com.ironcore.domain.user.repository.UserRepository;

import com.ironcore.domain.auth.valueobject.AuthSessionId;

import com.ironcore.application.exception.InvalidCredentialsException;

import com.ironcore.application.auth.port.RefreshTokenGenerator;

import com.ironcore.domain.auth.repository.AuthSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    private final AuthSessionRepository authSessionRepository;
    private final Clock clock;
    private final UserRepository userRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;

    @Transactional
    public void executeWithRefreshToken(String token) {
        AuthSessionId id;
        try {
            id = refreshTokenGenerator.sessionId(token);
        } catch (InvalidCredentialsException exception) {
            return;
        }
        var found = authSessionRepository.findUserIdById(id);
        if (found.isEmpty()) return;
        var owner = found.get();
        if (userRepository.findByIdForUpdate(owner).isEmpty()) return;
        authSessionRepository.findByIdAndUserId(id, owner)
                .filter(s -> s.isActiveAt(clock.instant()))
                .filter(s -> s.matchesRefreshCredential(refreshTokenGenerator.hash(token)))
                .ifPresent(s -> {
                    s.revoke(clock.instant());
                    authSessionRepository.save(s);
                });
    }

    @Transactional
    public void execute(LogoutCommand command) {
        if (command == null || userRepository.findByIdForUpdate(command.userId()).isEmpty()) return;
        authSessionRepository
                .findByIdAndUserId(command.sessionId(), command.userId())
                .ifPresent(session -> {
                    session.revoke(clock.instant());
                    authSessionRepository.save(session);
                });
    }
}
