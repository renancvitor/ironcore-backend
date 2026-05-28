package com.ironcore.application.user.service;

import com.ironcore.application.exception.BusinessRuleViolationException;
import com.ironcore.application.exception.InvalidCredentialsException;
import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.user.usecase.ChangePasswordCommand;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.RawPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class UserPasswordChangeService {

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;
    private final Clock clock;

    public User changePassword(ChangePasswordCommand command) {
        return changePassword(command, user -> {});
    }

    public User changePassword(
            User user,
            RawPassword currentPassword,
            RawPassword newPassword,
            RawPassword confirmPassword,
            Consumer<User> beforeChangePassword
    ) {
        if (!user.isActive()) {
            throw new OperationNotAllowedException("Usuário inativo.");
        }

        beforeChangePassword.accept(user);

        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessRuleViolationException("A confirmação de senha não confere.");
        }

        if (!passwordHashingService.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Senha atual inválida.");
        }

        if (passwordHashingService.matches(newPassword, user.getPasswordHash())) {
            throw new BusinessRuleViolationException("A nova senha deve ser diferente da senha atual.");
        }

        PasswordHash newPasswordHash = passwordHashingService.hash(newPassword);
        LocalDateTime updatedAt = LocalDateTime.now(clock);

        user.changePasswordHash(newPasswordHash, updatedAt);

        return userRepository.save(user);
    }

    public User changePassword(ChangePasswordCommand command, Consumer<User> beforePasswordChange) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new OperationNotAllowedException("Usuário inativo.");
        }

        beforePasswordChange.accept(user);

        if (!command.newPassword().equals(command.confirmPassword())) {
            throw new BusinessRuleViolationException("A confirmação de senha não confere.");
        }

        if (!passwordHashingService.matches(command.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Senha atual inválida.");
        }

        if (passwordHashingService.matches(command.newPassword(), user.getPasswordHash())) {
            throw new BusinessRuleViolationException("A nova senha deve ser diferente da senha atual.");
        }

        PasswordHash newPasswordHash = passwordHashingService.hash(command.newPassword());
        LocalDateTime updatedAt = LocalDateTime.now(clock);

        user.changePasswordHash(newPasswordHash, updatedAt);

        return userRepository.save(user);
    }
}
