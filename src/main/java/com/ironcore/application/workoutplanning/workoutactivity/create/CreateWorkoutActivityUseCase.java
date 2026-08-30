package com.ironcore.application.workoutplanning.workoutactivity.create;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.workoutplanning.workoutactivity.WorkoutActivityAuditData;
import com.ironcore.domain.exercise.exception.InvalidExerciseException;
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
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.repository.WorkoutDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateWorkoutActivityUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final WorkoutActivityRepository workoutActivityRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutCycleRepository workoutCycleRepository;
    private final ExerciseRepository exerciseRepository;
    private final Clock clock;
    private final AuditLogPublisher publisher;

    @Transactional
    public CreateWorkoutActivityResult execute(CreateWorkoutActivityCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        WorkoutDay workoutDay = workoutDayRepository
                .findByIdAndPersonId(command.workoutDayId(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dia de treino não encontrado."));

        WorkoutCycle workoutCycle = workoutCycleRepository
                .findByIdAndPersonId(workoutDay.getWorkoutCycleId(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo de treino não encontrado."));

        if (workoutCycle.getWorkoutStatus() == WorkoutStatus.COMPLETED
                || workoutCycle.getWorkoutStatus() == WorkoutStatus.CANCELLED) {
            throw new OperationNotAllowedException(
                    "Não é permitido adicionar atividades de treino de ciclos concluídos ou cancelados."
            );
        }

        Exercise exercise = exerciseRepository.findById(command.exerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercício não encontrado."));

        if (!exercise.getActive()) {
            throw new InvalidExerciseException("Exercício inativo.");
        }

        if (workoutActivityRepository.existsByPersonIdAndWorkoutDayIdAndExerciseId(
                person.getId(),
                workoutDay.getId(),
                exercise.getId()
        )) {
            throw new OperationNotAllowedException("Exercício já está vinculado a este dia de treino.");
        }

        int orderIndex = workoutActivityRepository
                .findByPersonIdAndWorkoutDayId(person.getId(), workoutDay.getId()).stream()
                .mapToInt(WorkoutActivity::getOrderIndex)
                .max()
                .orElse(0) + 1;

        LocalDateTime createdAt = LocalDateTime.now(clock);

        WorkoutActivity newWorkoutActivity = WorkoutActivity.register(
                workoutDay.getId(),
                exercise.getId(),
                orderIndex,
                command.sets(),
                command.repRangeMin(),
                command.repRangeMax(),
                command.targetLoadKg(),
                command.targetLoadText(),
                command.durationMinutes(),
                command.distanceKm(),
                command.intensityText(),
                command.restSeconds(),
                command.notes(),
                createdAt
        );

        WorkoutActivity savedWorkoutActivity = workoutActivityRepository.save(newWorkoutActivity);

        publisher.publish(
                AuditActionType.CREATE,
                user.getId().value(),
                user.getEmail().value(),
                AuditTargetType.WORKOUT_ACTIVITY,
                savedWorkoutActivity.getId().value(),
                null,
                WorkoutActivityAuditData.from(savedWorkoutActivity)
        );

        return new CreateWorkoutActivityResult(
                savedWorkoutActivity.getId(),
                savedWorkoutActivity.getWorkoutDayId(),
                savedWorkoutActivity.getExerciseId(),
                savedWorkoutActivity.getOrderIndex(),
                savedWorkoutActivity.getSets(),
                savedWorkoutActivity.getRepRangeMin(),
                savedWorkoutActivity.getRepRangeMax(),
                savedWorkoutActivity.getTargetLoadKg(),
                savedWorkoutActivity.getTargetLoadText(),
                savedWorkoutActivity.getDurationMinutes(),
                savedWorkoutActivity.getDistanceKm(),
                savedWorkoutActivity.getIntensityText(),
                savedWorkoutActivity.getRestSeconds(),
                savedWorkoutActivity.getNotes(),
                savedWorkoutActivity.getCreatedAt()
        );
    }
}
