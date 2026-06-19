package com.ironcore.application.userbodymetrics.delete;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.userbodymetrics.UserBodyMetricsAuditData;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.repository.UserBodyMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteUserBodyMetricsUseCase {

    private final UserRepository userRepository;
    private final UserBodyMetricsRepository userBodyMetricsRepository;
    private final AuditLogPublisher publisher;

    @Transactional
    public void execute(DeleteUserBodyMetricsCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        UserBodyMetrics userBodyMetrics = userBodyMetricsRepository
                .findByIdAndUserId(command.userBodyMetricsId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Métricas corporais não encontradas."));

        UserBodyMetricsAuditData beforeState = UserBodyMetricsAuditData.from(userBodyMetrics);

        userBodyMetricsRepository.deleteById(command.userBodyMetricsId());

        publisher.publish(
                AuditActionType.DELETE,
                user.getId().value(),
                user.getEmail().value(),
                AuditTargetType.USER_BODY_METRICS,
                userBodyMetrics.getId().value(),
                beforeState,
                null
        );
    }
}
