package com.ironcore.application.user.usecase;

import com.ironcore.application.auth.port.AccessTokenGenerator;
import com.ironcore.application.auth.port.AccessTokenSubject;
import com.ironcore.application.auth.port.GeneratedAccessToken;
import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.user.service.UserPasswordChangeService;
import com.ironcore.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InitialChangePasswordUseCase {

    private final UserPasswordChangeService userPasswordChangeService;
    private final AccessTokenGenerator accessTokenGenerator;

    @Transactional
    public InitialChangePasswordResult execute(ChangePasswordCommand command) {
        User user = userPasswordChangeService.changePassword(command, currentUser -> {
            if (!currentUser.mustChangePassword()) {
                throw new OperationNotAllowedException("A troca inicial de senha não é mais obrigatória.");
            }
        });

        GeneratedAccessToken accessToken = accessTokenGenerator.generate(
                new AccessTokenSubject(user.getId(), user.getEmail(), false)
        );

        return new InitialChangePasswordResult(
                accessToken.value(),
                accessToken.tokenType(),
                accessToken.expiresAt()
        );
    }
}
