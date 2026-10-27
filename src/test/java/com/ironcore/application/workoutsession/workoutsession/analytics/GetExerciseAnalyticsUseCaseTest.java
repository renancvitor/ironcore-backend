package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.application.exception.*;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.*;
import static com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetExerciseAnalyticsUseCaseTest {
    private final UserRepository users = mock(UserRepository.class);
    private final PersonRepository persons = mock(PersonRepository.class);
    private final ExerciseAnalyticsQueryPort queries = mock(ExerciseAnalyticsQueryPort.class);
    // UTC is already tomorrow; validation must use Sao Paulo's local date.
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-28T01:00:00Z"), ZoneOffset.UTC);
    private final GetExerciseAnalyticsUseCase useCase = new GetExerciseAnalyticsUseCase(users, persons, queries, clock);

    @ParameterizedTest
    @CsvSource({"2025-10-28,2026-10-27", "2026-10-27,2026-10-27", "2024-02-29,2025-02-27"})
    void shouldAcceptInclusivePeriodsAndUseAuthenticatedPerson(String start, String end) {
        owner();
        var command = command(start, end);
        when(queries.strength(restoredPerson().getId(), command.exerciseId(), command.startDate(), command.endDate()))
                .thenReturn(List.of());
        when(queries.cardio(restoredPerson().getId(), command.exerciseId(), command.startDate(), command.endDate()))
                .thenReturn(List.of());
        assertThat(useCase.strength(command).points()).isEmpty();
        var cardio = useCase.cardio(command);
        assertThat(cardio.points()).isEmpty();
        assertThat(cardio.summary().distanceKm()).isNull();
        assertThat(cardio.summary().durationMinutes()).isNull();
        verify(queries).strength(restoredPerson().getId(), command.exerciseId(), command.startDate(), command.endDate());
        verify(queries).cardio(restoredPerson().getId(), command.exerciseId(), command.startDate(), command.endDate());
    }

    @ParameterizedTest
    @CsvSource({"2025-10-27,2026-10-27", "2026-10-27,2026-10-26", "2026-10-27,2026-10-28",
            "2026-10-28,2026-10-28", "2024-02-29,2025-02-28"})
    void shouldRejectInvalidPeriodsBeforeQuery(String start, String end) {
        owner();
        assertThatThrownBy(() -> useCase.strength(command(start, end))).isInstanceOf(OperationNotAllowedException.class);
        assertThatThrownBy(() -> useCase.cardio(command(start, end))).isInstanceOf(OperationNotAllowedException.class);
        verifyNoInteractions(queries);
    }

    @Test
    void shouldRequireBothDatesForBothOperations() {
        owner();
        for (var command : List.of(command(null, "2026-10-27"), command("2026-10-27", null), command(null, null))) {
            assertThatThrownBy(() -> useCase.strength(command)).isInstanceOf(BusinessRuleViolationException.class);
            assertThatThrownBy(() -> useCase.cardio(command)).isInstanceOf(BusinessRuleViolationException.class);
        }
        verifyNoInteractions(queries);
    }

    @Test
    void shouldGroupActivitiesBySessionAndSummarizeRawCardioWithoutRoundingPointsFirst() {
        owner();
        var c = command("2026-10-27", "2026-10-27");
        var date = c.startDate();
        var time = Instant.parse("2026-10-27T12:00:00Z");
        when(queries.strength(any(), any(), any(), any())).thenReturn(List.of(
                new ExerciseAnalyticsQueryPort.StrengthRow(1L, date, time, "Old name", 1L, new BigDecimal("20"), TOTAL, 10),
                new ExerciseAnalyticsQueryPort.StrengthRow(1L, date, time, "Other snapshot", 2L, new BigDecimal("20"), PER_SIDE, 10),
                new ExerciseAnalyticsQueryPort.StrengthRow(2L, date, time, "New name", null, null, null, null)));
        var strength = useCase.strength(c);
        assertThat(strength.points()).hasSize(2);
        assertThat(strength.points().getFirst().exerciseNameSnapshot()).isEqualTo("Old name");
        assertThat(strength.points().getFirst().metrics().volumeKgReps()).isEqualByComparingTo("600");
        assertThat(strength.points().getLast().metrics().volumeKgReps()).isNull();

        when(queries.cardio(any(), any(), any(), any())).thenReturn(List.of(
                new ExerciseAnalyticsQueryPort.CardioRow(1L, date, time, "Old", 30, new BigDecimal("5")),
                new ExerciseAnalyticsQueryPort.CardioRow(1L, date, time, "Old", 100, null),
                new ExerciseAnalyticsQueryPort.CardioRow(2L, date, time, "New", 10, BigDecimal.ONE)));
        var cardio = useCase.cardio(c);
        assertThat(cardio.points()).hasSize(2);
        assertThat(cardio.points().getFirst().metrics().durationMinutes()).isEqualTo(130);
        assertThat(cardio.summary().averagePaceMinPerKm()).isEqualByComparingTo("6.67");
        assertThat(cardio.summary().averageSpeedKmPerHour()).isEqualByComparingTo("9");
    }

    @Test
    void shouldRejectMissingInactiveUserAndMissingPerson() {
        var c = command("2026-10-27", "2026-10-27");
        assertThatThrownBy(() -> useCase.strength(c)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> useCase.cardio(c)).isInstanceOf(ResourceNotFoundException.class);
        when(users.findById(c.actorUserId())).thenReturn(Optional.of(inactiveUser()));
        assertThatThrownBy(() -> useCase.strength(c)).isInstanceOf(UserInactiveException.class);
        assertThatThrownBy(() -> useCase.cardio(c)).isInstanceOf(UserInactiveException.class);
        when(users.findById(c.actorUserId())).thenReturn(Optional.of(activeUser()));
        assertThatThrownBy(() -> useCase.strength(c)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> useCase.cardio(c)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(queries);
    }

    private void owner() {
        when(users.findById(activeUser().getId())).thenReturn(Optional.of(activeUser()));
        when(persons.findById(activeUser().getPersonId())).thenReturn(Optional.of(restoredPerson()));
    }
    private ExerciseAnalyticsCommand command(String start, String end) {
        return new ExerciseAnalyticsCommand(activeUser().getId(), new ExerciseId(42L),
                start == null ? null : LocalDate.parse(start), end == null ? null : LocalDate.parse(end));
    }
}
