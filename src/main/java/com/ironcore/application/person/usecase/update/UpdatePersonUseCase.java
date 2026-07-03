package com.ironcore.application.person.usecase.update;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.person.usecase.PersonAuditData;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdatePersonUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final Clock clock;
    private final AuditLogPublisher publisher;

    @Transactional
    public UpdatePersonResult execute(UpdatePersonCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        if (command.name() == null
                && command.sex() == null
                && command.birthDate() == null) {
            throw new OperationNotAllowedException("Informe ao menos um campo para atualização.");
        }

        LocalDateTime updatedAt = LocalDateTime.now(clock);

        PersonAuditData beforeState = PersonAuditData.from(person);

        if (command.name() != null) {
            person.changeName(command.name(), updatedAt);
        }

        if (command.sex() != null) {
            person.changeSex(command.sex(), updatedAt);
        }

        if (command.birthDate() != null) {
            person.changeBirthDate(command.birthDate(), updatedAt);
        }

        Person savedPerson = personRepository.save(person);

        publisher.publish(
                AuditActionType.UPDATE,
                user.getId().value(),
                user.getEmail().value(),
                AuditTargetType.PERSON,
                savedPerson.getId().value(),
                beforeState,
                PersonAuditData.from(savedPerson)
        );

        return new UpdatePersonResult(
                savedPerson.getName(),
                savedPerson.getSex(),
                savedPerson.getBirthDate()
        );
    }
}
