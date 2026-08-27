package com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.repository;

import com.ironcore.application.workoutplanning.workoutcycle.detail.WorkoutCycleDetailProjection;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity.WorkoutCycleEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkoutCycleDetailJpaRepository
        extends Repository<WorkoutCycleEntity, Long> {

    @Query("""
            select new com.ironcore.application.workoutplanning.workoutcycle.detail.WorkoutCycleDetailProjection(
                wc.id,
                wc.name,
                wc.workoutStatus,
                wc.startDate,
                wc.endDate,
                wc.desiredDurationMonths,
                wc.notes,

                tg.id,
                tg.displayName,

                wd.id,
                wd.weekDay,
                wd.title,
                wd.sortOrder,

                wa.id,
                wa.orderIndex,
                wa.sets,
                wa.repRangeMin,
                wa.repRangeMax,
                wa.targetLoadKg,
                wa.targetLoadText,
                wa.durationMinutes,
                wa.distanceKm,
                wa.intensityText,
                wa.restSeconds,
                wa.notes,

                e.id,
                e.name,

                mg.id,
                mg.displayName
            )
            from WorkoutCycleEntity wc
            join wc.trainingGoal tg

            left join WorkoutDayEntity wd
                on wd.workoutCycle.id = wc.id

            left join WorkoutActivityEntity wa
                on wa.workoutDay.id = wd.id

            left join wa.exercise e

            left join ExerciseMuscleTargetEntity emt
                on emt.exercise.id = e.id
               and emt.active = true

            left join emt.muscleSubgroup ms

            left join ms.muscleGroup mg

            where wc.id = :workoutCycleId
              and wc.person.id = :personId

            order by
                wd.weekDay asc,
                wd.sortOrder asc,
                wa.orderIndex asc,
                mg.sortOrder asc
            """)
    List<WorkoutCycleDetailProjection> findDetail(
            @Param("workoutCycleId") Long workoutCycleId,
            @Param("personId") Long personId
    );
}
