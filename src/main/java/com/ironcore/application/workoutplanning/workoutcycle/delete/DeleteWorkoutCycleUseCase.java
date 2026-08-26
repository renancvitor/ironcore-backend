package com.ironcore.application.workoutplanning.workoutcycle.delete;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteWorkoutCycleUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final WorkoutCycleRepository workoutCycleRepository;

    @Transactional
    public void execute(DeleteWorkoutCycleCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        WorkoutCycle workoutCycle = workoutCycleRepository.findByIdAndPersonId(command.id(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo de treino não encontrado."));

        if (workoutCycle.getWorkoutStatus() != WorkoutStatus.NOT_STARTED) {
            throw new OperationNotAllowedException(
                    "Não é permitido excluir ciclos de treino iniciados, concluídos ou cancelados."
            );
        }

        workoutCycleRepository.deleteById(workoutCycle.getId());
    }
}
