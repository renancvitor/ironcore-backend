package com.ironcore.application.userbodymetrics.latest;

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
public class GetLatestUserBodyMetricsUseCase {

    private final UserRepository userRepository;
    private final UserBodyMetricsRepository userBodyMetricsRepository;

    @Transactional(readOnly = true)
    public GetLatestUserBodyMetricsResult execute(GetLatestUserBodyMetricsCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        UserBodyMetrics userBodyMetrics = userBodyMetricsRepository
                .findLatestByUserId(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Métricas corporais não encontradas."));

        return new GetLatestUserBodyMetricsResult(
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
