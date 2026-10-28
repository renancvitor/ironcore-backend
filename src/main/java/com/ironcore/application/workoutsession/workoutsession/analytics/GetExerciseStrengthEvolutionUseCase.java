package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.workoutsession.workoutsession.port.ExerciseAnalyticsQueryPort;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutsession.analytics.service.ExecutionMetricsCalculator;
import com.ironcore.domain.workoutsession.analytics.valueobject.SetExecution;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetExerciseStrengthEvolutionUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final ExerciseAnalyticsQueryPort queryPort;
    private final Clock clock;

    @Transactional(readOnly = true)
    public GetExerciseStrengthEvolutionResult execute(GetExerciseStrengthEvolutionCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        ExerciseAnalyticsPeriodValidator.validate(command.startDate(), command.endDate(), clock);

        List<ExerciseStrengthExecutionResult> executions = queryPort.findStrengthExecution(
                person.getId(), command.exerciseId(), command.startDate(), command.endDate()
        );
        Map<WorkoutSessionId, List<ExerciseStrengthExecutionResult>> executionsBySession = executions.stream()
                .collect(Collectors.groupingBy(
                        ExerciseStrengthExecutionResult::sessionId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<ExerciseStrengthEvolutionPointResult> points = executionsBySession.values().stream()
                .map(sessionExecutions -> {
                    var firstExecution = sessionExecutions.getFirst();
                    var sets = sessionExecutions.stream()
                            .filter(execution -> execution.setId() != null)
                            .map(execution -> new SetExecution(execution.loadKg(), execution.mode(), execution.reps()))
                            .toList();

                    return new ExerciseStrengthEvolutionPointResult(
                            firstExecution.sessionId(),
                            firstExecution.performedOn(),
                            firstExecution.startedAt(),
                            firstExecution.exerciseNameSnapshot(),
                            ExecutionMetricsCalculator.calculateStrength(sets),
                            firstExecution.workoutDayTitleSnapshot());
                })
                .toList();

        return new GetExerciseStrengthEvolutionResult(command.exerciseId(), points);
    }
}
