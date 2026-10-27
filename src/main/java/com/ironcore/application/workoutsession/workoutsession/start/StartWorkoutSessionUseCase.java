package com.ironcore.application.workoutsession.workoutsession.start;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.workoutsession.workoutsession.WorkoutSessionAuditData;
import com.ironcore.domain.activitytype.model.ActivityType;
import com.ironcore.domain.activitytype.repository.ActivityTypeRepository;
import com.ironcore.domain.exercise.model.Exercise;
import com.ironcore.domain.exercise.repository.ExerciseRepository;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutplanning.workoutactivity.repository.WorkoutActivityRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.repository.WorkoutDayRepository;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivitySource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StartWorkoutSessionUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final WorkoutCycleRepository workoutCycleRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutActivityRepository workoutActivityRepository;
    private final ExerciseRepository exerciseRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final Clock clock;
    private final AuditLogPublisher publisher;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StartWorkoutSessionResult execute(StartWorkoutSessionCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        WorkoutCycle cycle = workoutCycleRepository.findByIdAndPersonId(command.workoutCycleId(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo de treino não encontrado."));

        WorkoutDay day = workoutDayRepository.findByIdAndPersonId(command.workoutDayId(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dia de treino não encontrado."));

        List<WorkoutActivity> activities = workoutActivityRepository.findByPersonIdAndWorkoutDayId(
                person.getId(), day.getId());
        List<WorkoutSessionActivitySource> sources = new ArrayList<>();
        for (WorkoutActivity activity : activities) {
            Exercise exercise = exerciseRepository.findById(activity.getExerciseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Exercício não encontrado."));
            ActivityType activityType = activityTypeRepository.findById(exercise.getActivityTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de atividade não encontrado."));
            sources.add(new WorkoutSessionActivitySource(activity, exercise, activityType));
        }

        Instant now = clock.instant();
        WorkoutSession session = WorkoutSession.register(person.getId(), cycle, day, sources, now);
        WorkoutSession savedSession = workoutSessionRepository.save(session);

        publisher.publish(
                AuditActionType.CREATE,
                user.getId().value(),
                user.getEmail().value(),
                AuditTargetType.WORKOUT_SESSION,
                savedSession.getId().value(),
                null,
                WorkoutSessionAuditData.from(savedSession)
        );

        return new StartWorkoutSessionResult(
                savedSession.getId(),
                savedSession.getWorkoutCycleId(),
                savedSession.getWorkoutDayId(),
                savedSession.getPerformedOn(),
                savedSession.getStartedAt(),
                savedSession.getStatus(),
                savedSession.getCreatedAt()
        );
    }
}
