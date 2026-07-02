package com.ironcore.application.bodymetrics.create;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.bodymetrics.BodyMetricsAuditData;
import com.ironcore.application.bodymetrics.component.BodyFatPercentageCalculator;
import com.ironcore.domain.bodymetrics.valueobject.*;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.repository.BodyMetricsRepository;
import com.ironcore.domain.bodymetrics.service.BMICalculator;
import com.ironcore.domain.bodymetrics.service.FatMassCalculator;
import com.ironcore.domain.bodymetrics.service.LeanMassCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateBodyMetricsUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final BodyMetricsRepository bodyMetricsRepository;
    private final BMICalculator bmiCalculator;
    private final BodyFatPercentageCalculator bodyFatPercentageCalculator;
    private final FatMassCalculator fatMassCalculator;
    private final LeanMassCalculator leanMassCalculator;
    private final Clock clock;
    private final AuditLogPublisher publisher;

    @Transactional
    public CreateBodyMetricsResult execute(CreateBodyMetricsCommand command) {
        BodyCircumferences circumferences = command.circumferences();

        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        if (command.weight() == null || command.height() == null) {
            throw new OperationNotAllowedException("Peso e altura são obrigatórios.");
        }

        BMI bmi = bmiCalculator.calculate(command.height(), command.weight());

        BodyFatPercentage bodyFatPercentage = bodyFatPercentageCalculator.calculate(
                person,
                command.height(),
                circumferences
        );

        FatMassKg fatMassKg = null;
        if (bodyFatPercentage != null) {
            fatMassKg = fatMassCalculator.calculate(command.weight(), bodyFatPercentage);
        }

        LeanMassKg leanMassKg = null;
        if (fatMassKg != null) {
            leanMassKg = leanMassCalculator.calculate(command.weight(), fatMassKg);
        }

        LocalDateTime measuredAt = LocalDateTime.now(clock);

        BodyMetrics newBodyMetrics = BodyMetrics.register(
                person.getId(),
                measuredAt,
                command.weight(),
                command.height(),
                circumferences,
                bmi,
                bodyFatPercentage,
                fatMassKg,
                leanMassKg,
                command.notes()
        );

        BodyMetrics savedBodyMetrics = bodyMetricsRepository.save(newBodyMetrics);

        publisher.publish(
                AuditActionType.CREATE,
                user.getId().value(),
                user.getEmail().value(),
                AuditTargetType.BODY_METRICS,
                savedBodyMetrics.getId().value(),
                null,
                BodyMetricsAuditData.from(savedBodyMetrics)
        );

        return new CreateBodyMetricsResult(
                savedBodyMetrics.getId(),
                savedBodyMetrics.getPersonId(),
                savedBodyMetrics.getMeasuredAt(),
                savedBodyMetrics.getWeight(),
                savedBodyMetrics.getHeight(),
                savedBodyMetrics.getCircumferences(),
                savedBodyMetrics.getBmi(),
                savedBodyMetrics.getBodyFatPercentage(),
                savedBodyMetrics.getFatMassKg(),
                savedBodyMetrics.getLeanMassKg(),
                savedBodyMetrics.getNotes()
        );
    }
}
