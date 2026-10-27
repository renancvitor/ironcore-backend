package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.application.exception.*;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutsession.analytics.ExecutionMetrics;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetExerciseAnalyticsUseCase {
    private final UserRepository users;
    private final PersonRepository persons;
    private final ExerciseAnalyticsQueryPort queries;
    private final Clock clock;

    public ExerciseAnalyticsResult.Strength strength(ExerciseAnalyticsCommand command) {
        var owner = ownerAndValidate(command);
        var rows = queries.strength(owner, command.exerciseId(), command.startDate(), command.endDate());
        var groups = rows.stream().collect(Collectors.groupingBy(
                ExerciseAnalyticsQueryPort.StrengthRow::sessionId, LinkedHashMap::new, Collectors.toList()));
        var points = groups.values().stream().map(group -> {
            var first = group.getFirst();
            var sets = group.stream().filter(row -> row.setId() != null)
                    .map(row -> new ExecutionMetrics.SetExecution(row.loadKg(), row.mode(), row.reps())).toList();
            return new ExerciseAnalyticsResult.StrengthPoint(first.sessionId(), first.performedOn(),
                    first.startedAt(), first.exerciseNameSnapshot(), ExecutionMetrics.strength(sets));
        }).toList();
        return new ExerciseAnalyticsResult.Strength(command.exerciseId().value(), points);
    }

    public ExerciseAnalyticsResult.Cardio cardio(ExerciseAnalyticsCommand command) {
        var owner = ownerAndValidate(command);
        var rows = queries.cardio(owner, command.exerciseId(), command.startDate(), command.endDate());
        var groups = rows.stream().collect(Collectors.groupingBy(
                ExerciseAnalyticsQueryPort.CardioRow::sessionId, LinkedHashMap::new, Collectors.toList()));
        var points = groups.values().stream().map(group -> {
            var first = group.getFirst();
            var values = group.stream().map(row ->
                    new ExecutionMetrics.ActivityExecution(row.durationMinutes(), row.distanceKm())).toList();
            return new ExerciseAnalyticsResult.CardioPoint(first.sessionId(), first.performedOn(),
                    first.startedAt(), first.exerciseNameSnapshot(), ExecutionMetrics.cardio(values));
        }).toList();
        var all = rows.stream().map(row ->
                new ExecutionMetrics.ActivityExecution(row.durationMinutes(), row.distanceKm())).toList();
        return new ExerciseAnalyticsResult.Cardio(command.exerciseId().value(), points, ExecutionMetrics.cardio(all));
    }

    private PersonId ownerAndValidate(ExerciseAnalyticsCommand command) {
        var user = users.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        if (!user.isActive()) throw new UserInactiveException("Usuário inativo.");
        var person = persons.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));
        var start = command.startDate();
        var end = command.endDate();
        if (start == null || end == null) throw new BusinessRuleViolationException("As datas são obrigatórias.");
        if (start.isAfter(end)) throw new OperationNotAllowedException("Data inicial não pode ser posterior à data final.");
        if (end.isAfter(LocalDate.now(clock.withZone(WorkoutSession.ZONE))))
            throw new OperationNotAllowedException("Datas futuras não são permitidas.");
        if (end.isAfter(start.plusMonths(12).minusDays(1)))
            throw new OperationNotAllowedException("Período máximo permitido é de 12 meses.");
        return person.getId();
    }
}
