package com.ironcore.application.auth.usecase;

import com.ironcore.domain.auth.repository.AuthSessionRepository;
import com.ironcore.domain.user.repository.UserRepository;
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

    @Transactional
    public void execute(LogoutCommand command) {
        if (userRepository.findByIdForUpdate(command.userId()).isEmpty()) {
            return;
        }

        authSessionRepository
                .findByIdAndUserId(command.sessionId(), command.userId())
                .ifPresent(session -> {
                    session.revoke(clock.instant());
                    authSessionRepository.save(session);
                });
    }
}
