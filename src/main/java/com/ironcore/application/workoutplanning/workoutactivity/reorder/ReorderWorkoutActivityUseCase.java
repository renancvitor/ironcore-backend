package com.ironcore.application.workoutplanning.workoutactivity.reorder;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReorderWorkoutActivityUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final WorkoutActivityRepository workoutActivityRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutCycleRepository workoutCycleRepository;
    private final Clock clock;

    @Transactional
    public void execute(ReorderWorkoutActivityCommand command) {
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

        if (command.orderIndex() == null || command.orderIndex() <= 0) {
            throw new OperationNotAllowedException("Posição de ordenação inválida.");
        }

        List<WorkoutActivity> workoutActivities = workoutActivityRepository
                .findByPersonIdAndWorkoutDayId(person.getId(), workoutDay.getId());

        List<WorkoutActivity> reorderedActivities = workoutActivities.stream()
                .filter(activity -> !activity.getId().equals(command.id()))
                .sorted(Comparator.comparing(WorkoutActivity::getOrderIndex))
                .collect(Collectors.toCollection(ArrayList::new));

        int newIndex = command.orderIndex() - 1;

        if (newIndex > reorderedActivities.size()) {
            throw new OperationNotAllowedException("Posição de ordenação inválida.");
        }

        reorderedActivities.add(newIndex, workoutActivity);

        LocalDateTime updatedAt = LocalDateTime.now(clock);

        for (int i = 0; i < reorderedActivities.size(); i++) {
            reorderedActivities.get(i).reorder(
                    i + 1,
                    updatedAt
            );
        }

        reorderedActivities.forEach(workoutActivityRepository::save);
    }
}
