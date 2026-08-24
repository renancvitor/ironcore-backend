package com.ironcore.application.workoutplanning.workoutcycle.update;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.traininggoal.exception.InvalidTrainingGoalException;
import com.ironcore.domain.workoutplanning.traininggoal.model.TrainingGoal;
import com.ironcore.domain.workoutplanning.traininggoal.repository.TrainingGoalRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateWorkoutCycleUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final TrainingGoalRepository trainingGoalRepository;
    private final WorkoutCycleRepository workoutCycleRepository;
    private final Clock clock;

    @Transactional
    public UpdateWorkoutCycleResult execute(UpdateWorkoutCycleCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        WorkoutCycle workoutCycle = workoutCycleRepository.findByIdAndPersonId(command.id(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo de treino não encontrado."));

        if (workoutCycle.getWorkoutStatus() == WorkoutStatus.COMPLETED
                || workoutCycle.getWorkoutStatus() == WorkoutStatus.CANCELLED) {
            throw new OperationNotAllowedException(
                    "Não é permitido editar ciclos de treino concluídos ou cancelados."
            );
        }

        TrainingGoal trainingGoal = trainingGoalRepository.findById(command.trainingGoalId())
                .orElseThrow(() -> new ResourceNotFoundException("Objetivo de treino não encontrado."));

        if (!trainingGoal.getActive()) {
            throw new InvalidTrainingGoalException("Objetivo de treino inativo.");
        }

        LocalDateTime updatedAt = LocalDateTime.now(clock);

        workoutCycle.updateCycle(
                command.name(),
                trainingGoal.getId(),
                command.desiredDurationMonths(),
                command.notes(),
                updatedAt
        );

        WorkoutCycle savedWorkoutCycle = workoutCycleRepository.save(workoutCycle);

        return new UpdateWorkoutCycleResult(
                savedWorkoutCycle.getId(),
                savedWorkoutCycle.getName(),
                savedWorkoutCycle.getTrainingGoalId(),
                savedWorkoutCycle.getStartDate(),
                savedWorkoutCycle.getWorkoutStatus(),
                savedWorkoutCycle.getWorkoutOrigin(),
                savedWorkoutCycle.getDesiredDurationMonths(),
                savedWorkoutCycle.getNotes(),
                savedWorkoutCycle.getUpdatedAt()
        );
    }
}
