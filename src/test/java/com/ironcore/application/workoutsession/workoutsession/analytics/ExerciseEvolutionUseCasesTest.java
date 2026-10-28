package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.application.exception.BusinessRuleViolationException;
import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.workoutsession.workoutsession.port.ExerciseAnalyticsQueryPort;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode.PER_SIDE;
import static com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode.TOTAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseEvolutionUseCasesTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private ExerciseAnalyticsQueryPort queryPort;

    // Em UTC já é dia 28; em São Paulo ainda é dia 27.
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-28T01:00:00Z"), ZoneOffset.UTC);
    private GetExerciseStrengthEvolutionUseCase strengthUseCase;
    private GetExerciseCardioEvolutionUseCase cardioUseCase;

    @BeforeEach
    void setUp() {
        strengthUseCase = new GetExerciseStrengthEvolutionUseCase(userRepository, personRepository, queryPort, clock);
        cardioUseCase = new GetExerciseCardioEvolutionUseCase(userRepository, personRepository, queryPort, clock);
    }

    @ParameterizedTest
    @CsvSource({"2025-10-28,2026-10-27", "2026-10-27,2026-10-27", "2024-02-29,2025-02-27"})
    void shouldAcceptInclusivePeriodsAndUseAuthenticatedPerson(String start, String end) {
        givenPerson();
        var command = command(start, end);
        when(queryPort.findStrengthExecution(restoredPerson().getId(), command.exerciseId(), command.startDate(), command.endDate()))
                .thenReturn(List.of());
        when(queryPort.findCardioExecution(restoredPerson().getId(), command.exerciseId(), command.startDate(), command.endDate()))
                .thenReturn(List.of());
        var strength = strengthUseCase.execute(command);
        assertThat(strength.exerciseId()).isEqualTo(command.exerciseId());
        assertThat(strength.points()).isEmpty();
        var cardio = cardioUseCase.execute(toCardioCommand(command));
        assertThat(cardio.exerciseId()).isEqualTo(command.exerciseId());
        assertThat(cardio.points()).isEmpty();
        assertThat(cardio.summary().distanceKm()).isNull();
        assertThat(cardio.summary().durationMinutes()).isNull();
        verify(queryPort).findStrengthExecution(restoredPerson().getId(), command.exerciseId(), command.startDate(), command.endDate());
        verify(queryPort).findCardioExecution(restoredPerson().getId(), command.exerciseId(), command.startDate(), command.endDate());
    }

    @ParameterizedTest
    @CsvSource({"2025-10-27,2026-10-27", "2026-10-27,2026-10-26", "2026-10-27,2026-10-28",
            "2026-10-28,2026-10-28", "2024-02-29,2025-02-28"})
    void shouldRejectInvalidPeriodsBeforeQuery(String start, String end) {
        givenPerson();
        assertThatThrownBy(() -> strengthUseCase.execute(command(start, end))).isInstanceOf(OperationNotAllowedException.class);
        assertThatThrownBy(() -> cardioUseCase.execute(toCardioCommand(command(start, end)))).isInstanceOf(OperationNotAllowedException.class);
        verifyNoInteractions(queryPort);
    }

    @Test
    void shouldRequireBothDatesForBothOperations() {
        givenPerson();
        for (var command : List.of(command(null, "2026-10-27"), command("2026-10-27", null), command(null, null))) {
            assertThatThrownBy(() -> strengthUseCase.execute(command)).isInstanceOf(BusinessRuleViolationException.class);
            assertThatThrownBy(() -> cardioUseCase.execute(toCardioCommand(command))).isInstanceOf(BusinessRuleViolationException.class);
        }
        verifyNoInteractions(queryPort);
    }

    @Test
    void shouldGroupActivitiesBySessionAndSummarizeRawCardioWithoutRoundingPointsFirst() {
        givenPerson();
        var command = command("2026-10-27", "2026-10-27");
        var date = command.startDate();
        var time = Instant.parse("2026-10-27T12:00:00Z");
        when(queryPort.findStrengthExecution(any(), any(), any(), any())).thenReturn(List.of(
                new ExerciseStrengthExecutionResult(new WorkoutSessionId(1L), date, time, "Old name", new WorkoutSessionSetId(1L), new BigDecimal("20"), TOTAL, 10),
                new ExerciseStrengthExecutionResult(new WorkoutSessionId(1L), date, time, "Other snapshot", new WorkoutSessionSetId(2L), new BigDecimal("20"), PER_SIDE, 10),
                new ExerciseStrengthExecutionResult(new WorkoutSessionId(2L), date, time, "New name", null, null, null, null)));
        var strength = strengthUseCase.execute(command);
        assertThat(strength.points()).hasSize(2);
        assertThat(strength.points().getFirst().exerciseNameSnapshot()).isEqualTo("Old name");
        assertThat(strength.points().getFirst().metrics().volumeKgReps()).isEqualByComparingTo("600");
        assertThat(strength.points().getLast().metrics().volumeKgReps()).isNull();

        when(queryPort.findCardioExecution(any(), any(), any(), any())).thenReturn(List.of(
                new ExerciseCardioExecutionResult(new WorkoutSessionId(1L), date, time, "Old", 30, new BigDecimal("5")),
                new ExerciseCardioExecutionResult(new WorkoutSessionId(1L), date, time, "Old", 100, null),
                new ExerciseCardioExecutionResult(new WorkoutSessionId(2L), date, time, "New", 10, BigDecimal.ONE)));
        var cardio = cardioUseCase.execute(toCardioCommand(command));
        assertThat(cardio.points()).hasSize(2);
        assertThat(cardio.points().getFirst().metrics().durationMinutes()).isEqualTo(130);
        assertThat(cardio.summary().averagePaceMinPerKm()).isEqualByComparingTo("6.67");
        assertThat(cardio.summary().averageSpeedKmPerHour()).isEqualByComparingTo("9");
    }

    @Test
    void shouldRejectMissingInactiveUserAndMissingPerson() {
        var command = command("2026-10-27", "2026-10-27");
        assertThatThrownBy(() -> strengthUseCase.execute(command)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> cardioUseCase.execute(toCardioCommand(command))).isInstanceOf(ResourceNotFoundException.class);
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(inactiveUser()));
        assertThatThrownBy(() -> strengthUseCase.execute(command)).isInstanceOf(UserInactiveException.class);
        assertThatThrownBy(() -> cardioUseCase.execute(toCardioCommand(command))).isInstanceOf(UserInactiveException.class);
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(activeUser()));
        assertThatThrownBy(() -> strengthUseCase.execute(command)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> cardioUseCase.execute(toCardioCommand(command))).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(queryPort);
    }

    private void givenPerson() {
        when(userRepository.findById(activeUser().getId())).thenReturn(Optional.of(activeUser()));
        when(personRepository.findById(activeUser().getPersonId())).thenReturn(Optional.of(restoredPerson()));
    }

    private GetExerciseStrengthEvolutionCommand command(String start, String end) {
        return new GetExerciseStrengthEvolutionCommand(activeUser().getId(), new ExerciseId(42L),
                start == null ? null : LocalDate.parse(start), end == null ? null : LocalDate.parse(end));
    }

    private GetExerciseCardioEvolutionCommand toCardioCommand(GetExerciseStrengthEvolutionCommand command) {
        return new GetExerciseCardioEvolutionCommand(
                command.actorUserId(), command.exerciseId(), command.startDate(), command.endDate()
        );
    }
}
