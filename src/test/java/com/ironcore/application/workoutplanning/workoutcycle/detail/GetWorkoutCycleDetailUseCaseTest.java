package com.ironcore.application.workoutplanning.workoutcycle.detail;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.workoutplanning.workoutcycle.port.WorkoutCycleDetailQueryPort;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWorkoutCycleDetailUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private WorkoutCycleDetailQueryPort queryPort;

    @Mock
    private WorkoutCycleDetailAssembler assembler;

    private GetWorkoutCycleDetailUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetWorkoutCycleDetailUseCase(
                userRepository,
                personRepository,
                queryPort,
                assembler
        );
    }

    @Nested
    class SuccessfulGetWorkoutCycleDetail {

        @Test
        void shouldReturnDetailedWorkoutCycleForOwnedPerson() {
            User user = activeUser();
            Person person = restoredPerson();
            GetWorkoutCycleDetailCommand command = command(user);
            List<WorkoutCycleDetailProjection> projections = List.of(projection());
            WorkoutCycleDetailResult expected = org.mockito.Mockito.mock(WorkoutCycleDetailResult.class);

            when(userRepository.findById(command.actoruserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
            when(queryPort.findDetail(command.id(), person.getId())).thenReturn(projections);
            when(assembler.toResult(projections)).thenReturn(expected);

            WorkoutCycleDetailResult result = useCase.execute(command);

            assertThat(result).isSameAs(expected);
            verify(userRepository).findById(command.actoruserId());
            verify(personRepository).findById(user.getPersonId());
            verify(queryPort).findDetail(command.id(), person.getId());
            verify(assembler).toResult(projections);
        }
    }

    @Nested
    class AccessValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            GetWorkoutCycleDetailCommand command = command(new com.ironcore.domain.user.valueobject.UserId(1L));
            when(userRepository.findById(command.actoruserId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(queryPort, never()).findDetail(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
            verify(assembler, never()).toResult(org.mockito.ArgumentMatchers.any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            GetWorkoutCycleDetailCommand command = command(user);
            when(userRepository.findById(command.actoruserId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(personRepository, never()).findById(org.mockito.ArgumentMatchers.any());
            verify(queryPort, never()).findDetail(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            User user = activeUser();
            GetWorkoutCycleDetailCommand command = command(user);
            when(userRepository.findById(command.actoruserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Pessoa não encontrada.");

            verify(queryPort, never()).findDetail(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
            verify(assembler, never()).toResult(org.mockito.ArgumentMatchers.any());
        }

        @Test
        void shouldFailWhenWorkoutCycleDoesNotBelongToPerson() {
            User user = activeUser();
            Person person = restoredPerson();
            GetWorkoutCycleDetailCommand command = command(user);

            when(userRepository.findById(command.actoruserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
            when(queryPort.findDetail(command.id(), person.getId())).thenReturn(List.of());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Ciclo de treino não encontrado.");

            verify(queryPort).findDetail(command.id(), person.getId());
            verify(assembler, never()).toResult(org.mockito.ArgumentMatchers.any());
        }
    }

    private static GetWorkoutCycleDetailCommand command(User user) {
        return command(user.getId());
    }

    private static GetWorkoutCycleDetailCommand command(com.ironcore.domain.user.valueobject.UserId userId) {
        return new GetWorkoutCycleDetailCommand(userId, new WorkoutCycleId(10L));
    }

    private static WorkoutCycleDetailProjection projection() {
        return new WorkoutCycleDetailProjection(
                10L, "Hipertrofia", null, null, null, null, null,
                20L, "Hipertrofia", null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null
        );
    }
}
