package com.ironcore.application.auth.usecase;

import com.ironcore.application.auth.port.GeneratedAccessToken;
import com.ironcore.application.auth.port.RefreshTokenGenerator;
import com.ironcore.application.auth.service.AuthSessionIssuer;
import com.ironcore.application.exception.InvalidCredentialsException;
import com.ironcore.application.exception.RefreshConflictException;
import com.ironcore.domain.auth.model.AuthSession;
import com.ironcore.domain.auth.repository.AuthSessionRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {
    private final AuthSessionRepository sessions;
    private final UserRepository users;
    private final RefreshTokenGenerator tokens;
    private final AuthSessionIssuer issuer;
    private final Clock clock;

    @Transactional
    public GeneratedAccessToken execute(String token) {
        var id = tokens.sessionId(token);
        var owner = sessions.findUserIdById(id).orElseThrow(this::invalid);
        // All session mutations acquire the user lock first. Reload after acquiring it.
        User user = users.findByIdForUpdate(owner).orElseThrow(this::invalid);
        AuthSession session = sessions.findByIdAndUserId(id, owner).orElseThrow(this::invalid);
        if (!user.isActive() || user.mustChangePassword() || !session.isActiveAt(clock.instant())) {
            throw invalid();
        }
        String hash = tokens.hash(token);
        if (session.isRecentlyConsumedRefreshCredential(hash, clock.instant())) {
            throw new RefreshConflictException();
        }
        if (!session.matchesRefreshCredential(hash)) {
            throw invalid();
        }
        return issuer.renew(user, session);
    }

    private InvalidCredentialsException invalid() {
        return new InvalidCredentialsException("Credenciais inválidas.");
    }
}
