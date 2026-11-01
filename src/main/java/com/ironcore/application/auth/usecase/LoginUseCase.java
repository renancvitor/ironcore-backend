package com.ironcore.application.auth.usecase;


import com.ironcore.application.auth.port.GeneratedAccessToken;
import com.ironcore.application.auth.service.AuthSessionIssuer;
import com.ironcore.application.exception.InitialPasswordChangeRequiredException;
import com.ironcore.application.exception.InvalidCredentialsException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.user.service.PasswordHashingService;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.RawPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;
    private final AuthSessionIssuer authSessionIssuer;

    @Transactional
    public LoginResult execute(LoginCommand command) {
        Email email = command.email();
        RawPassword rawPassword = new RawPassword(command.rawPassword());

        User user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> {
                    passwordHashingService.verifyAgainstDummyHash(rawPassword);
                    return new InvalidCredentialsException("Credenciais inválidas.");
                });

        if (!passwordHashingService.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciais inválidas.");
        }

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        if (user.mustChangePassword()) {
            throw new InitialPasswordChangeRequiredException("Troca de senha inicial obrigatória.");
        }

        GeneratedAccessToken accessToken = authSessionIssuer.issue(user);

        return new LoginResult(
                accessToken.value(),
                accessToken.tokenType(),
                accessToken.expiresAt(),
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.mustChangePassword(),
                accessToken.refreshToken(),
                accessToken.refreshExpiresAt()
        );
    }
}
