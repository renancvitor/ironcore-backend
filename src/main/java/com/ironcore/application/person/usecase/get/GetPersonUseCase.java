package com.ironcore.application.person.usecase.get;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPersonUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;

    @Transactional(readOnly = true)
    public GetPersonResult execute(GetPersonCommand command) {
        User user = userRepository.findById(command.authenticatedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        return new GetPersonResult(
                person.getId(),
                person.getName(),
                person.getSex(),
                person.getBirthDate()
        );
    }
}
