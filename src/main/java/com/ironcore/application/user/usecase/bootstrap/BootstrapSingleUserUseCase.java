package com.ironcore.application.user.usecase.bootstrap;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.user.service.PasswordHashingService;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
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
    private final PersonRepository personRepository;

    @Transactional
    public void execute(BootstrapSingleUserCommand command) {
        Email email = command.email();

        if (userRepository.existsByEmail(email)) {
            return;
        }

        Person person = personRepository.findByName(command.personName())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa do bootstrap não encontrada."));

        if (userRepository.existsAny()) {
            throw new OperationNotAllowedException("Bootstrap de usuário único não pode criar outro usuário.");
        }

        RawPassword rawPassword = new RawPassword(command.rawPassword());
        PasswordHash passwordHash = passwordHashingService.hash(rawPassword);

        User user = User.register(
                command.nickname(),
                person.getId(),
                command.email(),
                passwordHash,
                command.createdAt()
        );

        userRepository.save(user);
    }
}
