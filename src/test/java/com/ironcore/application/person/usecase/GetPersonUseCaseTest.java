package com.ironcore.application.person.usecase;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.person.usecase.get.GetPersonCommand;
import com.ironcore.application.person.usecase.get.GetPersonResult;
import com.ironcore.application.person.usecase.get.GetPersonUseCase;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPersonUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private GetPersonUseCase useCase;

    @Test
    void shouldReturnPersonLinkedToAuthenticatedUser() {
        User user = activeUser();
        Person person = restoredPerson();
        GetPersonCommand command = new GetPersonCommand(user.getId());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));

        GetPersonResult result = useCase.execute(command);

        assertThat(result.personId()).isEqualTo(person.getId());
        assertThat(result.name()).isEqualTo(person.getName());
        assertThat(result.sex()).isEqualTo(person.getSex());
        assertThat(result.birthDate()).isEqualTo(person.getBirthDate());
        verify(userRepository).findById(user.getId());
        verify(personRepository).findById(user.getPersonId());
    }

    @Test
    void shouldFailWhenAuthenticatedUserIsNotFound() {
        GetPersonCommand command = new GetPersonCommand(new UserId(99L));

        when(userRepository.findById(command.authenticatedUserId())).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> useCase.execute(command))
                .withMessage("Usuário não encontrado.");

        verifyNoInteractions(personRepository);
    }

    @Test
    void shouldFailWhenAuthenticatedUserIsInactive() {
        User user = inactiveUser();
        GetPersonCommand command = new GetPersonCommand(user.getId());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatExceptionOfType(UserInactiveException.class)
                .isThrownBy(() -> useCase.execute(command))
                .withMessage("Usuário inativo.");

        verifyNoInteractions(personRepository);
    }

    @Test
    void shouldFailWhenLinkedPersonIsNotFound() {
        User user = activeUser();
        GetPersonCommand command = new GetPersonCommand(user.getId());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> useCase.execute(command))
                .withMessage("Pessoa não encontrada.");
    }
}
