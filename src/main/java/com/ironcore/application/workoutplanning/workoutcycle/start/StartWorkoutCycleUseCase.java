package com.ironcore.application.workoutplanning.workoutcycle.start;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.workoutplanning.workoutcycle.WorkoutCycleAuditData;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.traininggoal.exception.InvalidTrainingGoalException;
import com.ironcore.domain.workoutplanning.traininggoal.model.TrainingGoal;
import com.ironcore.domain.workoutplanning.traininggoal.repository.TrainingGoalRepository;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutplanning.workoutactivity.repository.WorkoutActivityRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.repository.WorkoutDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StartWorkoutCycleUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final TrainingGoalRepository trainingGoalRepository;
    private final WorkoutCycleRepository workoutCycleRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutActivityRepository workoutActivityRepository;
    private final Clock clock;
    private final AuditLogPublisher publisher;

    @Transactional
    public StartWorkoutCycleResult execute(StartWorkoutCycleCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        WorkoutCycle workoutCycle = workoutCycleRepository.findByIdAndPersonId(command.id(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo de treino não encontrado."));

        TrainingGoal trainingGoal = trainingGoalRepository.findById(workoutCycle.getTrainingGoalId())
                .orElseThrow(() -> new ResourceNotFoundException("Objetivo de treino não encontrado."));

        if (!trainingGoal.getActive()) {
            throw new InvalidTrainingGoalException("Objetivo de treino inativo.");
        }

        List<WorkoutDay> workoutDays = workoutDayRepository.findByWorkoutCycleId(workoutCycle.getId());

        if (workoutDays.isEmpty()) {
            throw new OperationNotAllowedException(
                    "O ciclo de treino deve possuir pelo menos um dia de treino."
            );
        }

        for (WorkoutDay workoutDay : workoutDays) {
            List<WorkoutActivity> activities = workoutActivityRepository.findByPersonIdAndWorkoutDayId(
                    person.getId(),
                    workoutDay.getId()
            );

            if (activities.isEmpty()) {
                throw new OperationNotAllowedException(
                        "Cada dia de treino deve possuir pelo menos uma atividade."
                );
            }
        }

        LocalDate startDate = LocalDate.now(clock);

        WorkoutCycleAuditData beforeState = WorkoutCycleAuditData.from(workoutCycle);

        workoutCycle.startCycle(startDate);

        WorkoutCycle savedWorkoutCycle = workoutCycleRepository.save(workoutCycle);

        publisher.publish(
                AuditActionType.UPDATE,
                user.getId().value(),
                user.getEmail().value(),
                AuditTargetType.WORKOUT_CYCLE,
                savedWorkoutCycle.getId().value(),
                beforeState,
                WorkoutCycleAuditData.from(savedWorkoutCycle)
        );

        return new StartWorkoutCycleResult(
                savedWorkoutCycle.getId(),
                savedWorkoutCycle.getTrainingGoalId(),
                savedWorkoutCycle.getStartDate(),
                savedWorkoutCycle.getWorkoutStatus()
        );
    }
}
