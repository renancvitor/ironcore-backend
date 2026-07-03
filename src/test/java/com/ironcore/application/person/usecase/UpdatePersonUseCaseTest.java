package com.ironcore.application.person.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.person.PersonAuditData;
import com.ironcore.application.person.usecase.update.UpdatePersonCommand;
import com.ironcore.application.person.usecase.update.UpdatePersonResult;
import com.ironcore.application.person.usecase.update.UpdatePersonUseCase;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.Sex;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePersonUseCaseTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 5, 10, 10, 0);
    private static final LocalDateTime EXISTING_UPDATED_AT = LocalDateTime.of(2026, 5, 10, 11, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 6, 20, 10, 0);
    private static final BirthDate EXISTING_BIRTH_DATE = new BirthDate(LocalDate.of(1994, 4, 9));

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private Clock clock;

    @Mock
    private AuditLogPublisher auditLogPublisher;

    private UpdatePersonUseCase updatePersonUseCase;

    @BeforeEach
    void setUp() {
        updatePersonUseCase = new UpdatePersonUseCase(
                userRepository,
                personRepository,
                clock,
                auditLogPublisher
        );
    }

    @Nested
    class SuccessfulUpdate {

        @Test
        void shouldUpdatePersonNameAndPreserveOtherFields() {
            User user = activeUser();
            UpdatePersonCommand command = new UpdatePersonCommand(
                    new UserId(1L),
                    "Renan Vitor",
                    null,
                    null
            );

            givenFixedClock();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            givenExistingPerson(user);
            givenUpdatedPersonIsPersisted();

            UpdatePersonResult result = updatePersonUseCase.execute(command);

            ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
            ArgumentCaptor<LoggableData> auditBeforeStateCaptor = ArgumentCaptor.forClass(LoggableData.class);
            ArgumentCaptor<LoggableData> auditAfterStateCaptor = ArgumentCaptor.forClass(LoggableData.class);

            verify(userRepository).findById(command.actorUserId());
            verify(personRepository).findById(user.getPersonId());
            verify(personRepository).save(personCaptor.capture());
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.UPDATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.PERSON),
                    eq(user.getPersonId().value()),
                    auditBeforeStateCaptor.capture(),
                    auditAfterStateCaptor.capture()
            );

            Person savedPerson = personCaptor.getValue();
            PersonAuditData auditBeforeState = (PersonAuditData) auditBeforeStateCaptor.getValue();
            PersonAuditData auditAfterState = (PersonAuditData) auditAfterStateCaptor.getValue();

            assertThat(savedPerson.getId()).isEqualTo(user.getPersonId());
            assertThat(savedPerson.getName()).isEqualTo("Renan Vitor");
            assertThat(savedPerson.getSex()).isEqualTo(new Sex(SexType.MALE));
            assertThat(savedPerson.getBirthDate()).isEqualTo(EXISTING_BIRTH_DATE);
            assertThat(savedPerson.getUpdatedAt()).isEqualTo(UPDATED_AT);

            assertThat(result.name()).isEqualTo("Renan Vitor");
            assertThat(result.sex()).isEqualTo(new Sex(SexType.MALE));
            assertThat(result.birthDate()).isEqualTo(EXISTING_BIRTH_DATE);

            assertThat(auditBeforeState.id()).isEqualTo(user.getPersonId().value());
            assertThat(auditBeforeState.name()).isEqualTo("Renan");
            assertThat(auditBeforeState.sex()).isEqualTo("MALE");
            assertThat(auditBeforeState.birthDate()).isEqualTo(EXISTING_BIRTH_DATE.value());

            assertThat(auditAfterState.id()).isEqualTo(user.getPersonId().value());
            assertThat(auditAfterState.name()).isEqualTo("Renan Vitor");
            assertThat(auditAfterState.sex()).isEqualTo("MALE");
            assertThat(auditAfterState.birthDate()).isEqualTo(EXISTING_BIRTH_DATE.value());
        }

        @Test
        void shouldUpdatePersonSexAndBirthDate() {
            User user = activeUser();
            BirthDate newBirthDate = new BirthDate(LocalDate.of(1995, 5, 10));
            UpdatePersonCommand command = new UpdatePersonCommand(
                    new UserId(1L),
                    null,
                    new Sex(SexType.FEMALE),
                    newBirthDate
            );

            givenFixedClock();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            givenExistingPerson(user);
            givenUpdatedPersonIsPersisted();

            UpdatePersonResult result = updatePersonUseCase.execute(command);

            ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);

            verify(personRepository).save(personCaptor.capture());
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.UPDATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.PERSON),
                    eq(user.getPersonId().value()),
                    any(PersonAuditData.class),
                    any(PersonAuditData.class)
            );

            Person savedPerson = personCaptor.getValue();

            assertThat(savedPerson.getName()).isEqualTo("Renan");
            assertThat(savedPerson.getSex()).isEqualTo(new Sex(SexType.FEMALE));
            assertThat(savedPerson.getBirthDate()).isEqualTo(newBirthDate);
            assertThat(savedPerson.getUpdatedAt()).isEqualTo(UPDATED_AT);

            assertThat(result.name()).isEqualTo("Renan");
            assertThat(result.sex()).isEqualTo(new Sex(SexType.FEMALE));
            assertThat(result.birthDate()).isEqualTo(newBirthDate);
        }
    }

    @Nested
    class UserValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            UpdatePersonCommand command = commandWithName();

            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> updatePersonUseCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(command.actorUserId());
            verify(personRepository, never()).findById(any());
            verify(personRepository, never()).save(any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            UpdatePersonCommand command = commandWithName();

            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> updatePersonUseCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(command.actorUserId());
            verify(personRepository, never()).findById(any());
            verify(personRepository, never()).save(any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    class PersonValidation {

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            User user = activeUser();
            UpdatePersonCommand command = commandWithName();

            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> updatePersonUseCase.execute(command))
                    .withMessage("Pessoa não encontrada.");

            verify(userRepository).findById(command.actorUserId());
            verify(personRepository).findById(user.getPersonId());
            verify(personRepository, never()).save(any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void shouldFailWhenNoUpdatableFieldIsProvided() {
            User user = activeUser();
            UpdatePersonCommand command = new UpdatePersonCommand(
                    new UserId(1L),
                    null,
                    null,
                    null
            );

            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            givenExistingPerson(user);

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> updatePersonUseCase.execute(command))
                    .withMessage("Informe ao menos um campo para atualização.");

            verify(userRepository).findById(command.actorUserId());
            verify(personRepository).findById(user.getPersonId());
            verify(personRepository, never()).save(any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }
    }

    private UpdatePersonCommand commandWithName() {
        return new UpdatePersonCommand(
                new UserId(1L),
                "Renan Vitor",
                null,
                null
        );
    }

    private void givenFixedClock() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-06-20T10:00:00Z"),
                ZoneOffset.UTC
        );

        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    private void givenExistingPerson(User user) {
        Person person = Person.restore(
                user.getPersonId(),
                "Renan",
                new Sex(SexType.MALE),
                EXISTING_BIRTH_DATE,
                CREATED_AT,
                EXISTING_UPDATED_AT
        );

        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
    }

    private void givenUpdatedPersonIsPersisted() {
        when(personRepository.save(any(Person.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }
}
