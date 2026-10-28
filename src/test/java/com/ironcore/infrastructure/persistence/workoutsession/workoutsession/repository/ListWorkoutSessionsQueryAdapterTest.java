package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutsession.workoutsession.enums.*;
import com.ironcore.infrastructure.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.time.*;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ListWorkoutSessionsQueryAdapterTest {
    @Mock private WorkoutSessionHistoryJpaRepository repository;
    @InjectMocks private ListWorkoutSessionsQueryAdapter adapter;

    @Test
    void shouldProjectNullableOriginsAndPreservePageMetadataAndOrdering() {
        var date = LocalDate.of(2026, 10, 26);
        var start = Instant.parse("2026-10-26T12:00:00Z");
        var item = new WorkoutSessionHistoryProjection(4L, null, null, "Original", "Treino A",
                WeekDay.MONDAY, date, start, start.plusSeconds(60),
                WorkoutSessionStatus.CLOSED, WorkoutSessionCloseReason.MANUAL);
        var pageable = PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "performedOn", "startedAt", "id"));
        when(repository.findClosedSessions(1L, 10L, 20L, date, date, pageable, null))
                .thenReturn(new PageImpl<>(List.of(item), pageable, 3));
        var result = adapter.findClosedSessions(new PersonId(1L), new WorkoutCycleId(10L),
                new WorkoutDayId(20L), date, date, new PageQuery(1, 2), null);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.last()).isTrue();
        assertThat(result.content()).singleElement().satisfies(row -> {
            assertThat(row.id().value()).isEqualTo(4L);
            assertThat(row.workoutCycleId()).isNull();
            assertThat(row.workoutDayId()).isNull();
            assertThat(row.workoutCycleNameSnapshot()).isEqualTo("Original");
            assertThat(row.finishedAt()).isEqualTo(item.finishedAt());
        });
    }

    @Test
    void shouldTranslateRepositoryFailure() {
        when(repository.findClosedSessions(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("offline"));
        assertThatThrownBy(() -> adapter.findClosedSessions(new PersonId(1L), null, null, null, null,
                new PageQuery(0, 20), null)).isInstanceOf(PersistenceException.class).hasRootCauseMessage("offline");
    }

    @Test
    void shouldTranslateInvalidProjection() {
        var invalid = new WorkoutSessionHistoryProjection(0L, null, null, "Original", "A",
                WeekDay.MONDAY, LocalDate.now(), Instant.now(), Instant.now(),
                WorkoutSessionStatus.CLOSED, WorkoutSessionCloseReason.MANUAL);
        when(repository.findClosedSessions(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(invalid)));
        assertThatThrownBy(() -> adapter.findClosedSessions(new PersonId(1L), null, null, null, null,
                new PageQuery(0, 20), null)).isInstanceOf(DataMappingException.class);
    }
}
