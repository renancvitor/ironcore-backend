package com.ironcore.application.workoutplanning.workoutcycle.cancel;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelWorkoutCycleUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final WorkoutCycleRepository workoutCycleRepository;

    @Transactional
    public CancelWorkoutCycleResult execute(CancelWorkoutCycleCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        WorkoutCycle workoutCycle = workoutCycleRepository.findByIdAndPersonId(command.id(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo de treino não encontrado."));

        workoutCycle.cancelCycle();

        WorkoutCycle savedWorkoutCycle = workoutCycleRepository.save(workoutCycle);

        return new CancelWorkoutCycleResult(
                savedWorkoutCycle.getId(),
                savedWorkoutCycle.getTrainingGoalId(),
                savedWorkoutCycle.getWorkoutStatus()
        );
    }
}
