package com.ironcore.application.person.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.person.usecase.bootstrap.BootstrapPersonCommand;
import com.ironcore.application.person.usecase.bootstrap.BootstrapPersonUseCase;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.person.valueobject.Sex;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.ironcore.application.person.BootstrapPersonTestFactory.command;
import static com.ironcore.application.person.BootstrapPersonTestFactory.existingPerson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BootstrapPersonUseCaseTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private BootstrapPersonUseCase useCase;

    @Nested
    class Idempotency {

        @Test
        void shouldDoNothingWhenConfiguredPersonAlreadyExists() {
            BootstrapPersonCommand command = command();

            Person existingPerson = existingPerson();

            when(personRepository.findByName(command.name()))
                    .thenReturn(Optional.of(existingPerson));

            useCase.execute(command);

            verify(personRepository).findByName(command.name());
            verify(personRepository, never()).existsAny();
            verify(personRepository, never()).save(any());
        }
    }

    @Nested
    class PersonViolation {

        @Test
        void shouldFailWhenAnotherPersonAlreadyExists() {
            BootstrapPersonCommand command = command();

            when(personRepository.findByName(command.name()))
                    .thenReturn(Optional.empty());
            when(personRepository.existsAny())
                    .thenReturn(true);

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Bootstrap de pessoa não pode criar outra pessoa.");

            verify(personRepository).existsAny();
            verify(personRepository, never()).save(any());
        }
    }

    @Nested
    class BootstrapCreation {

        @Test
        void shouldCreatePersonWhenNoPersonExists() {
            BootstrapPersonCommand command = command();

            when(personRepository.findByName(command.name()))
                    .thenReturn(Optional.empty());
            when(personRepository.existsAny())
                    .thenReturn(false);

            useCase.execute(command);

            ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);

            verify(personRepository).existsAny();
            verify(personRepository).save(personCaptor.capture());

            Person savedPerson =  personCaptor.getValue();

            assertThat(savedPerson.getName()).isEqualTo("Renan C Vitor");
            assertThat(savedPerson.getSex()).isEqualTo(new Sex(SexType.MALE));

        }
    }
}
