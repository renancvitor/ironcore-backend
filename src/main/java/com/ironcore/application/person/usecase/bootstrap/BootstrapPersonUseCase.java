package com.ironcore.application.person.usecase.bootstrap;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BootstrapPersonUseCase {

    private final PersonRepository personRepository;

    @Transactional
    public void execute(BootstrapPersonCommand command) {
        if (personRepository.findByName(command.name()).isPresent()) {
            return;
        }

        if (personRepository.existsAny()) {
            throw new OperationNotAllowedException("Bootstrap de pessoa não pode criar outra pessoa.");
        }

        Person person = Person.register(
                command.name(),
                command.sex(),
                command.birthDate(),
                command.createdAt()
        );

        personRepository.save(person);
    }
}
