package com.ironcore.application.auth.usecase;

import com.ironcore.application.auth.port.AccessTokenGenerator;
import com.ironcore.application.auth.port.AccessTokenSubject;
import com.ironcore.application.auth.port.GeneratedAccessToken;
import com.ironcore.application.exception.InvalidCredentialsException;
import com.ironcore.application.exception.OperationNotAllowedException;
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
            throw new OperationNotAllowedException("Usuário inativo.");
        }

        if (!passwordHashingService.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciais inválidas.");
        }

        GeneratedAccessToken accessToken = accessTokenGenerator.generate(
                new AccessTokenSubject(user.getId(), user.getEmail())
        );

        return new LoginResult(
                accessToken.value(),
                accessToken.tokenType(),
                accessToken.expiresAt(),
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.mustChangePassword()
        );
    }
}
