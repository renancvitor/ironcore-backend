package com.ironcore.application.workoutplanning.workoutday.reorder;

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
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.repository.WorkoutDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReorderWorkoutDayUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutCycleRepository workoutCycleRepository;
    private final Clock clock;
    private final AuditLogPublisher publisher;

    @Transactional
    public void execute(ReorderWorkoutDayCommand command) {
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

        if (command.weekDay() == null) {
            throw new OperationNotAllowedException("Dia da semana inválido.");
        }

        if (command.sortOrder() == null || command.sortOrder() <= 0) {
            throw new OperationNotAllowedException("Posição de ordenação inválida.");
        }

        List<WorkoutDay> workoutDays = workoutDayRepository.findByWorkoutCycleId(workoutCycle.getId());

        List<WorkoutDay> targetDay = workoutDays.stream()
                .filter(day -> day.getWeekDay() == command.weekDay())
                .filter(day -> !day.getId().equals(command.id()))
                .sorted(Comparator.comparing(WorkoutDay::getSortOrder))
                .collect(Collectors.toCollection(ArrayList::new));

        WeekDay sourceWeekDay = workoutDay.getWeekDay();
        boolean changeWeekDay = sourceWeekDay != command.weekDay();

        int newIndex = command.sortOrder() - 1;

        if (newIndex > targetDay.size()) {
            throw new OperationNotAllowedException("Posição de ordenação inválida.");
        }

        targetDay.add(newIndex, workoutDay);

        LocalDateTime updatedAt = LocalDateTime.now(clock);

        WorkoutDayAuditData beforeState = WorkoutDayAuditData.from(workoutDay);

        if (changeWeekDay) {
            List<WorkoutDay> sourceDay = workoutDays.stream()
                    .filter(day -> day.getWeekDay() == sourceWeekDay)
                    .filter(day -> !day.getId().equals(command.id()))
                    .sorted(Comparator.comparing(WorkoutDay::getSortOrder))
                    .collect(Collectors.toCollection(ArrayList::new));

            for (int i = 0; i < sourceDay.size(); i++) {
                sourceDay.get(i).reorder(
                        sourceWeekDay,
                        i + 1,
                        updatedAt
                );
            }

            sourceDay.forEach(workoutDayRepository::save);
        }

        for (int i = 0; i < targetDay.size(); i++) {
            targetDay.get(i).reorder(
                    command.weekDay(),
                    i + 1,
                    updatedAt
            );
        }

        targetDay.forEach(workoutDayRepository::save);

        WorkoutDayAuditData afterState = WorkoutDayAuditData.from(workoutDay);

        publisher.publish(
                AuditActionType.UPDATE,
                user.getId().value(),
                user.getEmail().value(),
                AuditTargetType.WORKOUT_DAY,
                workoutDay.getId().value(),
                beforeState,
                afterState
        );
    }
}
