package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionactivity.entity.WorkoutSessionActivityEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseAnalyticsJpaRepository extends Repository<WorkoutSessionActivityEntity, Long> {

    @Query("""
            select new com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository.ExerciseStrengthExecutionProjection(
                s.id, s.performedOn, s.startedAt, a.exerciseNameSnapshot,
                x.id, x.actualLoadKg, x.actualLoadMode, x.actualReps)
            from WorkoutSessionActivityEntity a join a.session s left join a.sets x
            where s.person.id = :personId and a.exerciseId = :exerciseId
              and s.status = com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus.CLOSED
              and s.performedOn between :startDate and :endDate
            order by s.performedOn, s.startedAt, s.id, a.orderIndexSnapshot, a.id, x.setNumber, x.id
            """)
    List<ExerciseStrengthExecutionProjection> findStrengthExecution(
            @Param("personId") Long personId,
            @Param("exerciseId") Long exerciseId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            select new com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository.ExerciseCardioExecutionProjection(
                s.id, s.performedOn, s.startedAt, a.exerciseNameSnapshot, a.actualDurationMinutes, a.actualDistanceKm)
            from WorkoutSessionActivityEntity a join a.session s
            where s.person.id = :personId and a.exerciseId = :exerciseId
              and s.status = com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus.CLOSED
              and s.performedOn between :startDate and :endDate
            order by s.performedOn, s.startedAt, s.id, a.orderIndexSnapshot, a.id
            """)
    List<ExerciseCardioExecutionProjection> findCardioExecution(
            @Param("personId") Long personId,
            @Param("exerciseId") Long exerciseId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
