package com.ironcore.application.workoutplanning.workoutcycle.list;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.workoutplanning.workoutcycle.port.ListWorkoutCyclesQueryPort;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListWorkoutCyclesUseCase {

    private final ListWorkoutCyclesQueryPort queryPort;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;

    @Transactional(readOnly = true)
    public ListWorkoutCyclesResult execute(ListWorkoutCyclesCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        if (command.startDate() != null
                && command.endDate() != null
                && command.startDate().isAfter(command.endDate())) {
            throw new OperationNotAllowedException("Data inicial não pode ser posterior à data final.");
        }

        PageQuery pageQuery = new PageQuery(
                command.page(),
                command.size()
        );

        PageResult<ListWorkoutCyclesItemResult> cycles =
                queryPort.findWorkoutCycles(
                        person.getId(),
                        command.workoutStatus(),
                        command.trainingGoalId(),
                        command.startDate(),
                        command.endDate(),
                        command.name(),
                        pageQuery
                );

        return new ListWorkoutCyclesResult(cycles);
    }
}
