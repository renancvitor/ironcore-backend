package com.ironcore.application.bodymetrics.delete;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.bodymetrics.BodyMetricsAuditData;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.repository.BodyMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteBodyMetricsUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final BodyMetricsRepository bodyMetricsRepository;
    private final AuditLogPublisher publisher;

    @Transactional
    public void execute(DeleteBodyMetricsCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        BodyMetrics bodyMetrics = bodyMetricsRepository
                .findByIdAndPersonId(command.bodyMetricsId(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Métricas corporais não encontradas."));

        BodyMetricsAuditData beforeState = BodyMetricsAuditData.from(bodyMetrics);

        bodyMetricsRepository.deleteById(command.bodyMetricsId());

        publisher.publish(
                AuditActionType.DELETE,
                user.getId().value(),
                user.getEmail().value(),
                AuditTargetType.BODY_METRICS,
                bodyMetrics.getId().value(),
                beforeState,
                null
        );
    }
}
