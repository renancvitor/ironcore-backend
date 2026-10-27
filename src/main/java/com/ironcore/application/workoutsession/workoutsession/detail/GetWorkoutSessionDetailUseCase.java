package com.ironcore.application.workoutsession.workoutsession.detail;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetWorkoutSessionDetailUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final WorkoutSessionRepository workoutSessionRepository;

    @Transactional(readOnly = true)
    public WorkoutSessionDetailResult execute(GetWorkoutSessionDetailCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        WorkoutSession session = workoutSessionRepository.findByIdAndPersonId(command.id(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sessão de treino não encontrada."));

        return WorkoutSessionDetailResult.from(session);
    }
}
