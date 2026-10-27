package com.ironcore.application.workoutsession.workoutsession.list;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.workoutsession.workoutsession.port.ListWorkoutSessionsQueryPort;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListWorkoutSessionsUseCase {

    private final ListWorkoutSessionsQueryPort queryPort;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;

    @Transactional(readOnly = true)
    public ListWorkoutSessionsResult execute(ListWorkoutSessionsCommand command) {
        var user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }
        var person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));
        if (command.startDate() != null && command.endDate() != null
                && command.startDate().isAfter(command.endDate())) {
            throw new OperationNotAllowedException("Data inicial não pode ser posterior à data final.");
        }
        var pageQuery = new PageQuery(command.page(), command.size());
        return new ListWorkoutSessionsResult(queryPort.findClosedSessions(
                person.getId(), command.workoutCycleId(), command.workoutDayId(),
                command.startDate(), command.endDate(), pageQuery));
    }
}
