package com.ironcore.application.auth.usecase;

import com.ironcore.domain.auth.repository.AuthSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    private final AuthSessionRepository authSessionRepository;
    private final Clock clock;

    public void execute(LogoutCommand command) {
        authSessionRepository
                .findByIdAndUserId(command.sessionId(), command.userId())
                .ifPresent(session -> {
                    session.revoke(clock.instant());
                    authSessionRepository.save(session);
                });
    }
}
