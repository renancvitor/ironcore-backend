package com.ironcore.application.workoutsession.workoutsession.persistence;

import com.ironcore.application.workoutsession.workoutsession.port.WorkoutSessionCreationPort;
import com.ironcore.application.workoutsession.workoutsession.port.WorkoutSessionPlanningQueryPort;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutsession.workoutsession.exception.WorkoutSessionConflictException;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import com.ironcore.domain.workoutsession.workoutsessionactivity.model.WorkoutSessionActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaveWorkoutSessionService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutSessionCreationPort workoutSessionCreationPort;
    private final WorkoutSessionPlanningQueryPort workoutSessionPlanningQueryPort;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public WorkoutSession save(WorkoutSession workoutSession) {
        workoutSession.validateExecution();
        if (workoutSession.getId() != null) {
            return workoutSessionRepository.save(workoutSession);
        }

        WorkoutSessionPlanningProjection planning = workoutSessionPlanningQueryPort.lockByDayIdAndPersonId(
                        workoutSession.getWorkoutDayId(), workoutSession.getPersonId())
                .orElseThrow(() -> new WorkoutSessionConflictException("Planejamento de origem indisponível."));
        validatePlanningSnapshot(workoutSession, planning);
        return workoutSessionCreationPort.insert(new ValidatedWorkoutSession(workoutSession));
    }

    private void validatePlanningSnapshot(WorkoutSession session, WorkoutSessionPlanningProjection planning) {
        if (!planning.personId().equals(session.getPersonId())
                || !planning.cycleId().equals(session.getWorkoutCycleId())
                || !planning.dayId().equals(session.getWorkoutDayId())
                || planning.status() != WorkoutStatus.IN_PROGRESS
                || !planning.snapshot().equals(session.getSnapshot())) {
            throw new WorkoutSessionConflictException("Planejamento de origem foi alterado. Recarregue a ficha.");
        }

        Map<Long, WorkoutSessionPlanningProjection.Activity> byId = planning.activities().stream()
                .collect(Collectors.toMap(activity -> activity.activityId().value(), Function.identity()));
        if (byId.size() != session.getActivities().size()) {
            throw new WorkoutSessionConflictException("Snapshot não corresponde à ficha completa do planejamento.");
        }
        for (WorkoutSessionActivity activity : session.getActivities()) {
            WorkoutSessionPlanningProjection.Activity planned = byId.get(activity.getWorkoutActivityId().value());
            if (planned == null || !planned.exerciseId().equals(activity.getExerciseId())) {
                throw new WorkoutSessionConflictException("Atividade de origem foi alterada.");
            }
            if (!planned.snapshot().equals(activity.getSnapshot())) {
                throw new WorkoutSessionConflictException("Prescrição foi alterada. Recarregue a ficha.");
            }
        }
    }
}
