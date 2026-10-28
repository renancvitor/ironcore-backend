package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.workoutsession.workoutsession.list.ListWorkoutSessionsItemResult;
import com.ironcore.application.workoutsession.workoutsession.port.ListWorkoutSessionsQueryPort;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.shared.pagination.PageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Locale;

@Repository
@RequiredArgsConstructor
public class ListWorkoutSessionsQueryAdapter implements ListWorkoutSessionsQueryPort {

    private final WorkoutSessionHistoryJpaRepository repository;

    private static String namePattern(String name) {
        if (name == null || name.isBlank()) return null;
        return "%" + name.strip().toLowerCase(Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    @Override
    public PageResult<ListWorkoutSessionsItemResult> findClosedSessions(
            PersonId personId, WorkoutCycleId workoutCycleId, WorkoutDayId workoutDayId,
            LocalDate startDate, LocalDate endDate, PageQuery pageQuery, String name) {
        Page<WorkoutSessionHistoryProjection> page;
        try {
            var pageable = PageRequest.of(pageQuery.page(), pageQuery.size(),
                    Sort.by(Sort.Direction.DESC, "performedOn", "startedAt", "id"));
            page = repository.findClosedSessions(personId.value(),
                    workoutCycleId == null ? null : workoutCycleId.value(),
                    workoutDayId == null ? null : workoutDayId.value(), startDate, endDate, pageable, namePattern(name));
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar histórico de sessões de treino.", exception);
        }
        try {
            return PageMapper.toPageResult(page.map(item -> new ListWorkoutSessionsItemResult(
                    new WorkoutSessionId(item.id()),
                    item.workoutCycleId() == null ? null : new WorkoutCycleId(item.workoutCycleId()),
                    item.workoutDayId() == null ? null : new WorkoutDayId(item.workoutDayId()),
                    item.workoutCycleNameSnapshot(), item.workoutDayTitleSnapshot(), item.weekDaySnapshot(),
                    item.performedOn(), item.startedAt(), item.finishedAt(), item.status(), item.closeReason())));
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao projetar histórico de sessões de treino.", exception);
        }
    }
}
