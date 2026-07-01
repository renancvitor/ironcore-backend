package com.ironcore.application.auth.usecase;

import com.ironcore.application.auth.port.AccessTokenGenerator;
import com.ironcore.application.auth.port.AccessTokenSubject;
import com.ironcore.application.auth.port.GeneratedAccessToken;
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

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;
    private final AccessTokenGenerator accessTokenGenerator;

    public LoginResult execute(LoginCommand command) {
        Email email = command.email();
        RawPassword rawPassword = new RawPassword(command.rawPassword());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Credenciais inválidas."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        if (!passwordHashingService.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciais inválidas.");
        }

        if (user.mustChangePassword()) {
            throw new InitialPasswordChangeRequiredException("Troca de senha inicial obrigatória.");
        }

        GeneratedAccessToken accessToken = accessTokenGenerator.generate(
                new AccessTokenSubject(user.getId(), user.getEmail(), user.mustChangePassword())
        );

        return new LoginResult(
                accessToken.value(),
                accessToken.tokenType(),
                accessToken.expiresAt(),
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.mustChangePassword()
        );
    }
}
