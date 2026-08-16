package com.ironcore.application.workoutplanning.workoutcycle.create;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.traininggoal.exception.InvalidTrainingGoalException;
import com.ironcore.domain.workoutplanning.traininggoal.model.TrainingGoal;
import com.ironcore.domain.workoutplanning.traininggoal.repository.TrainingGoalRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateWorkoutCycleUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final TrainingGoalRepository trainingGoalRepository;
    private final WorkoutCycleRepository workoutCycleRepository;
    private final Clock clock;

    @Transactional
    public CreateWorkoutCycleResult execute(CreateWorkoutCycleCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        TrainingGoal trainingGoal = trainingGoalRepository.findById(command.trainingGoalId())
                .orElseThrow(() -> new ResourceNotFoundException("Objetivo de treino não encontrado."));

        if (!trainingGoal.getActive()) {
            throw new InvalidTrainingGoalException("Objetivo de treino inativo.");
        }

        LocalDateTime createdAt = LocalDateTime.now(clock);

        WorkoutCycle newWorkoutCycle = WorkoutCycle.register(
                person.getId(),
                command.name(),
                trainingGoal.getId(),
                command.desiredDurationMonths(),
                WorkoutOrigin.MANUAL,
                command.notes(),
                createdAt
        );

        WorkoutCycle savedWorkoutCycle = workoutCycleRepository.save(newWorkoutCycle);

        return new CreateWorkoutCycleResult(
                savedWorkoutCycle.getId(),
                savedWorkoutCycle.getPersonId(),
                savedWorkoutCycle.getName(),
                savedWorkoutCycle.getTrainingGoalId(),
                savedWorkoutCycle.getDesiredDurationMonths(),
                savedWorkoutCycle.getWorkoutStatus(),
                savedWorkoutCycle.getWorkoutOrigin(),
                savedWorkoutCycle.getNotes(),
                savedWorkoutCycle.getCreatedAt()
        );
    }
}
