package com.ironcore.application.bodymetrics.latest;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.repository.BodyMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetLatestBodyMetricsUseCase {

    private final UserRepository userRepository;
    private final BodyMetricsRepository bodyMetricsRepository;

    @Transactional(readOnly = true)
    public GetLatestBodyMetricsResult execute(GetLatestBodyMetricsCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        BodyMetrics bodyMetrics = bodyMetricsRepository
                .findLatestByPersonId(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Métricas corporais não encontradas."));

        return new GetLatestBodyMetricsResult(
                bodyMetrics.getId(),
                bodyMetrics.getPersonId(),
                bodyMetrics.getMeasuredAt(),
                bodyMetrics.getWeight(),
                bodyMetrics.getHeight(),
                bodyMetrics.getCircumferences(),
                bodyMetrics.getBmi(),
                bodyMetrics.getBodyFatPercentage(),
                bodyMetrics.getFatMassKg(),
                bodyMetrics.getLeanMassKg(),
                bodyMetrics.getNotes(),
                bodyMetrics.getUpdatedAt()
        );
    }
}
