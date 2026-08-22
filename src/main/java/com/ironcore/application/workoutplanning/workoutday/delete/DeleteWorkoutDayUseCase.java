package com.ironcore.application.workoutplanning.workoutday.delete;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
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
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeleteWorkoutDayUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutCycleRepository workoutCycleRepository;
    private final Clock clock;

    @Transactional
    public void execute(DeleteWorkoutDayCommand command) {
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
                    "Não é permitido excluir dias de treino de ciclos concluídos ou cancelados."
            );
        }

        WeekDay deletedWeekDay = workoutDay.getWeekDay();

        workoutDayRepository.deleteById(workoutDay.getId());

        List<WorkoutDay> remainingWorkoutDays = workoutDayRepository
                .findByWorkoutCycleId(workoutCycle.getId())
                .stream()
                .filter(day -> day.getWeekDay() == deletedWeekDay)
                .sorted(Comparator.comparing(WorkoutDay::getSortOrder))
                .toList();

        LocalDateTime updatedAt = LocalDateTime.now(clock);

        for (int i = 0; i < remainingWorkoutDays.size(); i++) {
            remainingWorkoutDays.get(i).reorder(
                    deletedWeekDay,
                    i + 1,
                    updatedAt
            );
        }

        remainingWorkoutDays.forEach(workoutDayRepository::save);
    }
}
