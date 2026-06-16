package com.ironcore.application.userbodymetrics.update;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.userbodymetrics.UserBodyMetricsAuditData;
import com.ironcore.application.userbodymetrics.component.BodyFatPercentageCalculator;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
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
public class UpdateUserBodyMetricsUseCase {

    private final UserRepository userRepository;
    private final UserBodyMetricsRepository userBodyMetricsRepository;
    private final BMICalculator bmiCalculator;
    private final BodyFatPercentageCalculator bodyFatPercentageCalculator;
    private final FatMassCalculator fatMassCalculator;
    private final LeanMassCalculator leanMassCalculator;
    private final Clock clock;
    private final AuditLogPublisher publisher;

    @Transactional
    public UpdateUserBodyMetricsResult execute(UpdateUserBodyMetricsCommand command) {
        BodyCircumferences circumferences = command.circumferences();

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        if (command.weight() == null || command.height() == null) {
            throw new OperationNotAllowedException("Peso e altura são obrigatórios.");
        }

        BMI bmi = bmiCalculator.calculate(command.height(), command.weight());

        BodyFatPercentage bodyFatPercentage = bodyFatPercentageCalculator.calculate(
                user,
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

        LocalDateTime updatedAt = LocalDateTime.now(clock);

        UserBodyMetrics userBodyMetrics = userBodyMetricsRepository
                .findByIdAndUserId(command.userBodyMetricsId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Métricas corporais não encontradas."));

        UserBodyMetricsAuditData beforeState = UserBodyMetricsAuditData.from(userBodyMetrics);

        userBodyMetrics.updateMeasurements(
                command.weight(),
                command.height(),
                circumferences,
                bmi,
                bodyFatPercentage,
                fatMassKg,
                leanMassKg,
                command.notes(),
                updatedAt
        );

        UserBodyMetrics savedUserBodyMetrics = userBodyMetricsRepository.save(userBodyMetrics);

        publisher.publish(
                AuditActionType.UPDATE,
                user.getId().value(),
                user.getEmail().value(),
                AuditTargetType.USER_BODY_METRICS,
                savedUserBodyMetrics.getId().value(),
                beforeState,
                UserBodyMetricsAuditData.from(savedUserBodyMetrics)
        );

        return new UpdateUserBodyMetricsResult(
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
                savedUserBodyMetrics.getNotes(),
                savedUserBodyMetrics.getUpdatedAt()
        );
    }
}
