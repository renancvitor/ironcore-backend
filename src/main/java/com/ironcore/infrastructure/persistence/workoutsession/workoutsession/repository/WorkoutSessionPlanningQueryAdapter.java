package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.application.workoutsession.workoutsession.persistence.WorkoutSessionPlanningProjection;
import com.ironcore.application.workoutsession.workoutsession.port.WorkoutSessionPlanningQueryPort;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutsession.workoutsession.exception.WorkoutSessionConflictException;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionSnapshot;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivitySnapshot;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class WorkoutSessionPlanningQueryAdapter implements WorkoutSessionPlanningQueryPort {

    private final EntityManager entityManager;

    @Override
    public Optional<WorkoutSessionPlanningProjection> lockByDayIdAndPersonId(WorkoutDayId dayId, PersonId personId) {
        try {
            List<?> cycles = entityManager.createNativeQuery("""
                    SELECT c.id FROM workout_cycles c JOIN workout_days d ON d.workout_cycle_id = c.id
                    WHERE d.id = :day AND c.person_id = :person FOR UPDATE OF c
                    """).setParameter("day", dayId.value()).setParameter("person", personId.value()).getResultList();
            if (cycles.isEmpty()) {
                return Optional.empty();
            }
            Long cycleId = ((Number) cycles.getFirst()).longValue();
            List<?> days = entityManager.createNativeQuery("""
                    SELECT id FROM workout_days WHERE id = :day AND workout_cycle_id = :cycle FOR UPDATE
                    """).setParameter("day", dayId.value()).setParameter("cycle", cycleId).getResultList();
            if (days.isEmpty()) {
                return Optional.empty();
            }

            // FOR UPDATE on the day also blocks new activities through the planning FK.
            lockRows("""
                    SELECT id FROM workout_activities WHERE workout_day_id = :day ORDER BY id FOR UPDATE
                    """, dayId);
            lockRows("""
                    SELECT e.id FROM exercises e
                    WHERE e.id IN (SELECT exercise_id FROM workout_activities WHERE workout_day_id = :day)
                    ORDER BY e.id FOR SHARE
                    """, dayId);
            lockRows("""
                    SELECT t.id FROM activity_types t
                    WHERE t.id IN (
                        SELECT e.activity_type_id FROM exercises e JOIN workout_activities a ON a.exercise_id = e.id
                        WHERE a.workout_day_id = :day)
                    ORDER BY t.id FOR SHARE
                    """, dayId);

            Object[] header = (Object[]) entityManager.createNativeQuery("""
                    SELECT c.name, c.workout_status, d.title, d.week_day, current_setting('transaction_isolation')
                    FROM workout_cycles c JOIN workout_days d ON d.workout_cycle_id = c.id
                    WHERE d.id = :day AND c.id = :cycle AND c.person_id = :person
                    """).setParameter("day", dayId.value()).setParameter("cycle", cycleId)
                    .setParameter("person", personId.value()).getSingleResult();

            // A joined REPEATABLE READ transaction can hide inserts committed before the day lock.
            if ("repeatable read".equals(header[4])) {
                throw new WorkoutSessionConflictException("Não foi possível validar a prescrição nesta transação. Tente novamente.");
            }

            List<?> rows = entityManager.createNativeQuery("""
                    SELECT a.id, e.id, e.name, t.code, a.order_index, a.sets, a.rep_range_min, a.rep_range_max,
                        a.target_load_kg, a.target_load_text, a.duration_minutes, a.distance_km,
                        a.intensity_text, a.rest_seconds, a.notes
                    FROM workout_activities a JOIN exercises e ON e.id = a.exercise_id
                    JOIN activity_types t ON t.id = e.activity_type_id
                    WHERE a.workout_day_id = :day ORDER BY a.order_index
                    """).setParameter("day", dayId.value()).getResultList();

            return Optional.of(toProjection(personId, cycleId, dayId, header, rows));
        } catch (ConcurrencyFailureException | PessimisticLockException | LockTimeoutException
                 | LockAcquisitionException exception) {
            throw new WorkoutSessionConflictException("Planejamento foi alterado por outra operação.", exception);
        } catch (WorkoutSessionConflictException | DataMappingException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao consultar e bloquear a prescrição da sessão.", exception);
        }
    }

    private void lockRows(String sql, WorkoutDayId dayId) {
        entityManager.createNativeQuery(sql).setParameter("day", dayId.value()).getResultList();
    }

    private WorkoutSessionPlanningProjection toProjection(
            PersonId personId,
            Long cycleId,
            WorkoutDayId dayId,
            Object[] header,
            List<?> rows
    ) {
        try {
            List<WorkoutSessionPlanningProjection.Activity> activities = rows.stream()
                    .map(row -> toActivity((Object[]) row)).toList();
            return new WorkoutSessionPlanningProjection(personId, new WorkoutCycleId(cycleId), dayId,
                    WorkoutStatus.valueOf((String) header[1]),
                    new WorkoutSessionSnapshot(((String) header[0]).trim(), ((String) header[2]).trim(),
                            WeekDay.fromValue(((Number) header[3]).intValue())), activities);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao mapear prescrição da sessão.", exception);
        }
    }

    private WorkoutSessionPlanningProjection.Activity toActivity(Object[] row) {
        return new WorkoutSessionPlanningProjection.Activity(
                new WorkoutActivityId(((Number) row[0]).longValue()), new ExerciseId(((Number) row[1]).longValue()),
                new WorkoutSessionActivitySnapshot(((String) row[2]).trim(), new ActivityTypeCode((String) row[3]).value(), (Integer) row[4],
                        (Integer) row[5], (Integer) row[6], (Integer) row[7], (BigDecimal) row[8],
                        trimmedText(row[9]), (Integer) row[10], (BigDecimal) row[11], trimmedText(row[12]),
                        (Integer) row[13], trimmedText(row[14])));
    }

    private String trimmedText(Object value) {
        return value == null ? null : ((String) value).trim();
    }
}
