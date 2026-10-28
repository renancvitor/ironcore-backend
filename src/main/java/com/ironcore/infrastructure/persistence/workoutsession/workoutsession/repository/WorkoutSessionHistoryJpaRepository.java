package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.entity.WorkoutSessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface WorkoutSessionHistoryJpaRepository extends Repository<WorkoutSessionEntity, Long> {

    @Query(value = """
            select new com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository.WorkoutSessionHistoryProjection(
                s.id, s.workoutCycleId, s.workoutDayId,
                s.workoutCycleNameSnapshot, s.workoutDayTitleSnapshot, s.weekDaySnapshot,
                s.performedOn, s.startedAt, s.finishedAt, s.status, s.closeReason)
            from WorkoutSessionEntity s
            where s.person.id = :personId
              and s.status = com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus.CLOSED
              and (:name is null
                   or lower(s.workoutCycleNameSnapshot) like :name escape '!'
                   or lower(s.workoutDayTitleSnapshot) like :name escape '!')
              and (:cycleId is null or s.workoutCycleId = :cycleId)
              and (:dayId is null or s.workoutDayId = :dayId)
              and (cast(:startDate as date) is null or s.performedOn >= :startDate)
              and (cast(:endDate as date) is null or s.performedOn <= :endDate)
            """, countQuery = """
            select count(s.id)
            from WorkoutSessionEntity s
            where s.person.id = :personId
              and s.status = com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus.CLOSED
              and (:name is null
                   or lower(s.workoutCycleNameSnapshot) like :name escape '!'
                   or lower(s.workoutDayTitleSnapshot) like :name escape '!')
              and (:cycleId is null or s.workoutCycleId = :cycleId)
              and (:dayId is null or s.workoutDayId = :dayId)
              and (cast(:startDate as date) is null or s.performedOn >= :startDate)
              and (cast(:endDate as date) is null or s.performedOn <= :endDate)
            """)
    Page<WorkoutSessionHistoryProjection> findClosedSessions(
            @Param("personId") Long personId,
            @Param("cycleId") Long cycleId,
            @Param("dayId") Long dayId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable,
            @Param("name") String name);
}
