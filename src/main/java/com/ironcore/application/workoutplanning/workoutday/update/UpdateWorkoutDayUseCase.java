package com.ironcore.application.workoutplanning.workoutday.update;

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
public class UpdateWorkoutDayUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutCycleRepository workoutCycleRepository;
    private final Clock clock;
    private final AuditLogPublisher publisher;

    @Transactional
    public UpdateWorkoutDayResult execute(UpdateWorkoutDayCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        WorkoutDay workoutDay = workoutDayRepository.findByIdAndPersonId(command.id(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dia de treino não encontrado."));

        WorkoutCycle workoutCycle = workoutCycleRepository
                .findByIdAndPersonId(workoutDay.getWorkoutCycleId(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo de treino não encontrado."));

        if (workoutCycle.getWorkoutStatus() == WorkoutStatus.COMPLETED
                || workoutCycle.getWorkoutStatus() == WorkoutStatus.CANCELLED) {
            throw new OperationNotAllowedException(
                    "Não é permitido editar dias de treino de ciclos concluídos ou cancelados."
            );
        }

        LocalDateTime updatedAt =  LocalDateTime.now(clock);

        WorkoutDayAuditData beforeState = WorkoutDayAuditData.from(workoutDay);

        workoutDay.updateDay(
                command.title(),
                updatedAt
        );

        WorkoutDay savedWorkoutDay = workoutDayRepository.save(workoutDay);

        publisher.publish(
                AuditActionType.UPDATE,
                user.getId().value(),
                user.getEmail().value(),
                AuditTargetType.WORKOUT_DAY,
                savedWorkoutDay.getId().value(),
                beforeState,
                WorkoutDayAuditData.from(savedWorkoutDay)
        );

        return new UpdateWorkoutDayResult(
                savedWorkoutDay.getId(),
                savedWorkoutDay.getWorkoutCycleId(),
                savedWorkoutDay.getWeekDay(),
                savedWorkoutDay.getTitle(),
                savedWorkoutDay.getSortOrder(),
                savedWorkoutDay.getUpdatedAt()
        );
    }
}
