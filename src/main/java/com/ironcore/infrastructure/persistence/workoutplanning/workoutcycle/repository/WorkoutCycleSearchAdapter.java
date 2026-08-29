package com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.repository;

import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.workoutplanning.workoutcycle.list.ListWorkoutCyclesItemResult;
import com.ironcore.application.workoutplanning.workoutcycle.list.TrainingGoalItemResult;
import com.ironcore.application.workoutplanning.workoutcycle.port.ListWorkoutCyclesQueryPort;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.shared.pagination.PageMapper;
import com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.entity.TrainingGoalEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity.WorkoutCycleEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.specification.WorkoutCycleSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class WorkoutCycleSearchAdapter implements ListWorkoutCyclesQueryPort {

    private final WorkoutCycleJpaRepository workoutCycleJpaRepository;

    @Override
    public PageResult<ListWorkoutCyclesItemResult> findWorkoutCycles(
            PersonId personId,
            WorkoutStatus workoutStatus,
            TrainingGoalId trainingGoalId,
            LocalDate startDate,
            LocalDate endDate,
            String name,
            PageQuery pageQuery
    ) {
        Page<WorkoutCycleEntity> entities;

        try {
            Specification<WorkoutCycleEntity> specification =
                    WorkoutCycleSpecification.filter(
                            personId.value(),
                            workoutStatus,
                            valueOf(trainingGoalId),
                            startDate,
                            endDate,
                            name
                    );

            Pageable pageable = PageRequest.of(
                    pageQuery.page(),
                    pageQuery.size(),
                    Sort.by("name").ascending()
                            .and(Sort.by("id").ascending())
            );

            entities = workoutCycleJpaRepository.findAll(specification, pageable);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar ciclos.", exception);
        }

        try {
            Page<ListWorkoutCyclesItemResult> results = entities.map(WorkoutCycleSearchAdapter::toResult);

            return PageMapper.toPageResult(results);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao projetar ciclos.", exception);
        }
    }

    private static Long valueOf(TrainingGoalId id) {
        return id == null ? null : id.value();
    }

    private static ListWorkoutCyclesItemResult toResult(WorkoutCycleEntity entity) {
        return new ListWorkoutCyclesItemResult(
                new WorkoutCycleId(entity.getId()),
                entity.getName(),
                entity.getWorkoutStatus(),
                toResult(entity.getTrainingGoal()),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getDesiredDurationMonths()
        );
    }

    private static TrainingGoalItemResult toResult(TrainingGoalEntity entity) {
        return new TrainingGoalItemResult(
                new TrainingGoalId(entity.getId()),
                entity.getDisplayName()
        );
    }
}
