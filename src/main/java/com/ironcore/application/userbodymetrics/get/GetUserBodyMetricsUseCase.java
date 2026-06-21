package com.ironcore.application.userbodymetrics.get;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.repository.UserBodyMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserBodyMetricsUseCase {

    private final UserRepository userRepository;
    private final UserBodyMetricsRepository userBodyMetricsRepository;

    @Transactional(readOnly = true)
    public GetUserBodyMetricsResult execute(GetUserBodyMetricsCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        UserBodyMetrics userBodyMetrics = userBodyMetricsRepository
                .findByIdAndUserId(command.userBodyMetricsId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Métricas corporais não encontradas."));

        return new GetUserBodyMetricsResult(
                userBodyMetrics.getId(),
                userBodyMetrics.getUserId(),
                userBodyMetrics.getMeasuredAt(),
                userBodyMetrics.getWeight(),
                userBodyMetrics.getHeight(),
                userBodyMetrics.getCircumferences(),
                userBodyMetrics.getBmi(),
                userBodyMetrics.getBodyFatPercentage(),
                userBodyMetrics.getFatMassKg(),
                userBodyMetrics.getLeanMassKg(),
                userBodyMetrics.getNotes(),
                userBodyMetrics.getUpdatedAt()
        );
    }
}
