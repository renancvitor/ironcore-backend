package com.ironcore.application.user.usecase.initialchangepassword;

import com.ironcore.application.exception.InvalidCredentialsException;
import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.user.service.UserPasswordChangeService;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InitialChangePasswordUseCase {

    private final UserPasswordChangeService userPasswordChangeService;
    private final UserRepository userRepository;

    @Transactional
    public void execute(InitialChangePasswordCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciais inválidas."));

        userPasswordChangeService.changePassword(
                user,
                command.currentPassword(),
                command.newPassword(),
                command.confirmPassword(),
                currentUser -> {
                    if (!currentUser.mustChangePassword()) {
                        throw new OperationNotAllowedException("A troca inicial de senha não é mais obrigatória.");
                    }
                }
        );
    }
}
