package com.ironcore.application.workoutsession.workoutsession.list;

import com.ironcore.application.exception.*;
import com.ironcore.application.shared.pagination.*;
import com.ironcore.application.workoutsession.workoutsession.port.ListWorkoutSessionsQueryPort;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.*;
import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListWorkoutSessionsUseCaseTest {
    @Mock private ListWorkoutSessionsQueryPort queryPort;
    @Mock private UserRepository userRepository;
    @Mock private PersonRepository personRepository;
    @InjectMocks private ListWorkoutSessionsUseCase useCase;

    @Test
    void shouldResolveOwnerAndForwardAllFiltersAndPagination() {
        givenPerson();
        var date = LocalDate.of(2026, 10, 26);
        var command = new ListWorkoutSessionsCommand(activeUser().getId(), new WorkoutCycleId(10L),
                new WorkoutDayId(20L), date, date, 1, 2, "Treino");
        var page = new PageResult<ListWorkoutSessionsItemResult>(List.of(), 1, 2, 2, 1, true);
        when(queryPort.findClosedSessions(restoredPerson().getId(), command.workoutCycleId(),
                command.workoutDayId(), date, date, new PageQuery(1, 2), "Treino")).thenReturn(page);
        assertThat(useCase.execute(command).sessions()).isSameAs(page);
        verify(queryPort).findClosedSessions(restoredPerson().getId(), command.workoutCycleId(),
                command.workoutDayId(), date, date, new PageQuery(1, 2), "Treino");
    }

    @Test
    void shouldRejectMissingUser() {
        assertThatThrownBy(() -> useCase.execute(command(0, 20))).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(personRepository, queryPort);
    }

    @Test
    void shouldRejectInactiveUser() {
        when(userRepository.findById(activeUser().getId())).thenReturn(Optional.of(inactiveUser()));
        assertThatThrownBy(() -> useCase.execute(command(0, 20))).isInstanceOf(UserInactiveException.class);
        verifyNoInteractions(personRepository, queryPort);
    }

    @Test
    void shouldRejectMissingPerson() {
        when(userRepository.findById(activeUser().getId())).thenReturn(Optional.of(activeUser()));
        assertThatThrownBy(() -> useCase.execute(command(0, 20))).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(queryPort);
    }

    @Test
    void shouldRejectReversedInterval() {
        givenPerson();
        assertThatThrownBy(() -> useCase.execute(new ListWorkoutSessionsCommand(activeUser().getId(),
                null, null, LocalDate.of(2026, 10, 27), LocalDate.of(2026, 10, 26), 0, 20, null)))
                .isInstanceOf(OperationNotAllowedException.class);
        verifyNoInteractions(queryPort);
    }

    @ParameterizedTest
    @CsvSource({"-1,20", "0,0", "0,101"})
    void shouldRejectInvalidPaginationBeforeQuery(int page, int size) {
        givenPerson();
        assertThatThrownBy(() -> useCase.execute(command(page, size))).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(queryPort);
    }

    private void givenPerson() {
        when(userRepository.findById(activeUser().getId())).thenReturn(Optional.of(activeUser()));
        when(personRepository.findById(activeUser().getPersonId())).thenReturn(Optional.of(restoredPerson()));
    }

    private ListWorkoutSessionsCommand command(int page, int size) {
        return new ListWorkoutSessionsCommand(activeUser().getId(), null, null, null, null, page, size, null);
    }
}
