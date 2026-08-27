package com.ironcore.application.workoutplanning.workoutcycle.detail;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.workoutplanning.workoutcycle.port.WorkoutCycleDetailQueryPort;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetWorkoutCycleDetailUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final WorkoutCycleDetailQueryPort workoutCycleDetailQueryPort;
    private final WorkoutCycleDetailAssembler workoutCycleDetailAssembler;

    @Transactional(readOnly = true)
    public WorkoutCycleDetailResult execute(GetWorkoutCycleDetailCommand command) {
        User user = userRepository.findById(command.actoruserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        List<WorkoutCycleDetailProjection> projection = workoutCycleDetailQueryPort
                .findDetail(command.id(), person.getId());

        if (projection.isEmpty()) {
            throw new ResourceNotFoundException("Ciclo de treino não encontrado.");
        }

        return workoutCycleDetailAssembler.toResult(projection);
    }
}
