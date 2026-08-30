package com.ironcore.application.workoutplanning.workoutday.create;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.workoutplanning.workoutday.WorkoutDayAuditData;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
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
public class CreateWorkoutDayUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final WorkoutCycleRepository workoutCycleRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final Clock clock;
    private final AuditLogPublisher publisher;

    @Transactional
    public CreateWorkoutDayResult execute(CreateWorkoutDayCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        WorkoutCycle workoutCycle = workoutCycleRepository.findByIdAndPersonId(command.workoutCycleId(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo de treino não encontrado."));

        if (workoutCycle.getWorkoutStatus() == WorkoutStatus.COMPLETED
                || workoutCycle.getWorkoutStatus() == WorkoutStatus.CANCELLED) {
            throw new OperationNotAllowedException(
                    "Não é permitido adicionar dias de treino de ciclos concluídos ou cancelados."
            );
        }

        int sortOrder = workoutDayRepository.findByWorkoutCycleId(workoutCycle.getId()).stream()
                .filter(day -> day.getWeekDay() == command.weekDay())
                .mapToInt(WorkoutDay::getSortOrder)
                .max()
                .orElse(0) + 1;

        LocalDateTime createdAt = LocalDateTime.now(clock);

        WorkoutDay newWorkoutDay = WorkoutDay.register(
                workoutCycle.getId(),
                command.weekDay(),
                command.title(),
                sortOrder,
                createdAt
        );

        WorkoutDay savedWorkoutDay = workoutDayRepository.save(newWorkoutDay);

        publisher.publish(
                AuditActionType.CREATE,
                user.getId().value(),
                user.getEmail().value(),
                AuditTargetType.WORKOUT_DAY,
                savedWorkoutDay.getId().value(),
                null,
                WorkoutDayAuditData.from(savedWorkoutDay)
        );

        return new CreateWorkoutDayResult(
                savedWorkoutDay.getId(),
                savedWorkoutDay.getWorkoutCycleId(),
                savedWorkoutDay.getWeekDay(),
                savedWorkoutDay.getTitle(),
                savedWorkoutDay.getSortOrder(),
                savedWorkoutDay.getCreatedAt()
        );
    }
}
