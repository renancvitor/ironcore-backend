package com.ironcore.application.userbodymetrics.create;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.userbodymetrics.UserBodyMetricsAuditData;
import com.ironcore.application.userbodymetrics.component.BodyFatPercentageCalculator;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.repository.UserBodyMetricsRepository;
import com.ironcore.domain.userbodymetrics.service.BMICalculator;
import com.ironcore.domain.userbodymetrics.service.FatMassCalculator;
import com.ironcore.domain.userbodymetrics.service.LeanMassCalculator;
import com.ironcore.domain.userbodymetrics.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateUserBodyMetricsUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final UserBodyMetricsRepository userBodyMetricsRepository;
    private final BMICalculator bmiCalculator;
    private final BodyFatPercentageCalculator bodyFatPercentageCalculator;
    private final FatMassCalculator fatMassCalculator;
    private final LeanMassCalculator leanMassCalculator;
    private final Clock clock;
    private final AuditLogPublisher publisher;

    @Transactional
    public CreateUserBodyMetricsResult execute(CreateUserBodyMetricsCommand command) {
        BodyCircumferences circumferences = command.circumferences();

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

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

        UserBodyMetrics newUserBodyMetrics = UserBodyMetrics.register(
                command.userId(),
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

        UserBodyMetrics savedUserBodyMetrics = userBodyMetricsRepository.save(newUserBodyMetrics);

        publisher.publish(
                AuditActionType.CREATE,
                user.getId().value(),
                user.getEmail().value(),
                AuditTargetType.USER_BODY_METRICS,
                savedUserBodyMetrics.getId().value(),
                null,
                UserBodyMetricsAuditData.from(savedUserBodyMetrics)
        );

        return new CreateUserBodyMetricsResult(
                savedUserBodyMetrics.getId(),
                savedUserBodyMetrics.getUserId(),
                savedUserBodyMetrics.getMeasuredAt(),
                savedUserBodyMetrics.getWeight(),
                savedUserBodyMetrics.getHeight(),
                savedUserBodyMetrics.getCircumferences(),
                savedUserBodyMetrics.getBmi(),
                savedUserBodyMetrics.getBodyFatPercentage(),
                savedUserBodyMetrics.getFatMassKg(),
                savedUserBodyMetrics.getLeanMassKg(),
                savedUserBodyMetrics.getNotes()
        );
    }
}
