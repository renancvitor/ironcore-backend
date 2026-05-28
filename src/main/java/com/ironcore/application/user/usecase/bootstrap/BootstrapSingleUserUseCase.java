package com.ironcore.application.user.usecase.bootstrap;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.user.service.PasswordHashingService;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.RawPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BootstrapSingleUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;

    @Transactional
    public void execute(BootstrapSingleUserCommand command) {
        Email email = command.email();

        if (userRepository.existsByEmail(email)) {
            return;
        }

        if (userRepository.existsAny()) {
            throw new OperationNotAllowedException("Bootstrap de usuário único não pode criar outro usuário.");
        }

        RawPassword rawPassword = new RawPassword(command.rawPassword());
        PasswordHash passwordHash = passwordHashingService.hash(rawPassword);

        User user = User.register(
                command.name(),
                command.email(),
                passwordHash,
                command.sex(),
                command.createdAt()
        );

        userRepository.save(user);
    }
}
