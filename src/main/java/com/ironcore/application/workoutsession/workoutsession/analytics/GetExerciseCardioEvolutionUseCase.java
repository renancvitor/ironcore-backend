package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.workoutsession.workoutsession.port.ExerciseAnalyticsQueryPort;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutsession.analytics.service.ExecutionMetricsCalculator;
import com.ironcore.domain.workoutsession.analytics.valueobject.ActivityExecution;
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
public class GetExerciseCardioEvolutionUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final ExerciseAnalyticsQueryPort queryPort;
    private final Clock clock;

    @Transactional(readOnly = true)
    public GetExerciseCardioEvolutionResult execute(GetExerciseCardioEvolutionCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        ExerciseAnalyticsPeriodValidator.validate(command.startDate(), command.endDate(), clock);

        List<ExerciseCardioExecutionResult> executions = queryPort.findCardioExecution(
                person.getId(), command.exerciseId(), command.startDate(), command.endDate()
        );
        Map<Long, List<ExerciseCardioExecutionResult>> executionsBySession = executions.stream()
                .collect(Collectors.groupingBy(
                        ExerciseCardioExecutionResult::sessionId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<ExerciseCardioEvolutionPointResult> points = executionsBySession.values().stream()
                .map(sessionExecutions -> {
                    var firstExecution = sessionExecutions.getFirst();
                    var activities = sessionExecutions.stream()
                            .map(execution -> new ActivityExecution(execution.durationMinutes(), execution.distanceKm()))
                            .toList();

                    return new ExerciseCardioEvolutionPointResult(
                            firstExecution.sessionId(),
                            firstExecution.performedOn(),
                            firstExecution.startedAt(),
                            firstExecution.exerciseNameSnapshot(),
                            ExecutionMetricsCalculator.calculateCardio(activities)
                    );
                })
                .toList();

        List<ActivityExecution> activities = executions.stream()
                .map(execution -> new ActivityExecution(execution.durationMinutes(), execution.distanceKm()))
                .toList();

        return new GetExerciseCardioEvolutionResult(
                command.exerciseId().value(), points, ExecutionMetricsCalculator.calculateCardio(activities)
        );
    }
}
