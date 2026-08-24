package com.ironcore.application.workoutplanning.workoutactivity.update;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.domain.exercise.exception.InvalidExerciseException;
import com.ironcore.domain.exercise.model.Exercise;
import com.ironcore.domain.exercise.repository.ExerciseRepository;
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
public class UpdateWorkoutActivityUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final WorkoutActivityRepository workoutActivityRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutCycleRepository workoutCycleRepository;
    private final ExerciseRepository exerciseRepository;
    private final Clock clock;

    @Transactional
    public UpdateWorkoutActivityResult execute(UpdateWorkoutActivityCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        WorkoutActivity workoutActivity = workoutActivityRepository.findByIdAndPersonId(command.id(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Atividade de treino não encontrada."));

        WorkoutDay workoutDay = workoutDayRepository
                .findByIdAndPersonId(workoutActivity.getWorkoutDayId(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dia de treino não encontrado."));

        WorkoutCycle workoutCycle = workoutCycleRepository
                .findByIdAndPersonId(workoutDay.getWorkoutCycleId(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo de treino não encontrado."));

        if (workoutCycle.getWorkoutStatus() == WorkoutStatus.COMPLETED
                || workoutCycle.getWorkoutStatus() == WorkoutStatus.CANCELLED) {
            throw new OperationNotAllowedException(
                    "Não é permitido editar atividades de treino de ciclos concluídos ou cancelados."
            );
        }

        Exercise exercise = exerciseRepository.findById(command.exerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercício não encontrado."));

        if (!exercise.getActive()) {
            throw new InvalidExerciseException("Exercício inativo.");
        }

        if (workoutActivityRepository.existsByPersonIdAndWorkoutDayIdAndExerciseIdExcludingId(
                person.getId(),
                workoutDay.getId(),
                exercise.getId(),
                workoutActivity.getId()
        )) {
            throw new OperationNotAllowedException("Exercício já está vinculado a este dia de treino.");
        }

        LocalDateTime updatedAt = LocalDateTime.now(clock);

        workoutActivity.updateActivity(
                exercise.getId(),
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
                updatedAt
        );

        WorkoutActivity savedWorkoutActivity = workoutActivityRepository.save(workoutActivity);

        return new UpdateWorkoutActivityResult(
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
                savedWorkoutActivity.getUpdatedAt()
        );
    }
}
