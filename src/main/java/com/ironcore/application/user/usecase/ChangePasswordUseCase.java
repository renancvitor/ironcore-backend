package com.ironcore.application.user.usecase;

import com.ironcore.application.exception.BusinessRuleViolationException;
import com.ironcore.application.exception.InvalidCredentialsException;
import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.user.service.PasswordHashingService;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.PasswordHash;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;
    private final Clock clock;

    @Transactional
    public void execute(ChangePasswordCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new OperationNotAllowedException("Usuário inativo.");
        }

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

        userRepository.save(user);
    }
}
