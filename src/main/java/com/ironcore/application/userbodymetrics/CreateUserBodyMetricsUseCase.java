package com.ironcore.application.userbodymetrics;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.domain.user.enums.SexType;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.repository.UserBodyMetricsRepository;
import com.ironcore.domain.userbodymetrics.service.BMICalculator;
import com.ironcore.domain.userbodymetrics.service.FatMassCalculator;
import com.ironcore.domain.userbodymetrics.service.LeanMassCalculator;
import com.ironcore.domain.userbodymetrics.service.NavyBodyFatCalculator;
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
    private final UserBodyMetricsRepository userBodyMetricsRepository;
    private final BMICalculator bmiCalculator;
    private final NavyBodyFatCalculator navyBodyFatCalculator;
    private final FatMassCalculator fatMassCalculator;
    private final LeanMassCalculator leanMassCalculator;
    private final Clock clock;

    @Transactional
    public CreateUserBodyMetricsResult execute(CreateUserBodyMetricsCommand command) {
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

        BodyFatPercentage bodyFatPercentage = calculateBodyFatPercentage(
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

    private BodyFatPercentage calculateBodyFatPercentage(
            User user,
            BodyHeightCm height,
            BodyCircumferences circumferences
    ) {
        if (circumferences == null) {
            return null;
        }

        if (user.getSex().type() == SexType.MALE
                && circumferences.neck() != null
                && circumferences.waist() != null) {
            return navyBodyFatCalculator.calculate(user.getSex().type(), height, circumferences);
        }

        if (user.getSex().type() == SexType.FEMALE
                && circumferences.neck() != null
                && circumferences.waist() != null
                && circumferences.hip() != null) {
            return navyBodyFatCalculator.calculate(user.getSex().type(), height, circumferences);
        }

        return null;
    }
}
