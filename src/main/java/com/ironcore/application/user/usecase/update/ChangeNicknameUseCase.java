package com.ironcore.application.user.usecase.update;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.user.UserAuditData;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChangeNicknameUseCase {

    private final UserRepository userRepository;
    private final AuditLogPublisher publisher;
    private final Clock clock;

    @Transactional
    public ChangeNicknameResult execute(ChangeNicknameCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        if (command.nickname() == null) {
            throw new OperationNotAllowedException("Informe o apelido para atualização.");
        }

        LocalDateTime updatedAt = LocalDateTime.now(clock);

        UserAuditData beforeState = UserAuditData.from(user);

        user.changeNickname(command.nickname(), updatedAt);

        User savedUser = userRepository.save(user);

        publisher.publish(
                AuditActionType.UPDATE,
                user.getId().value(),
                user.getEmail().value(),
                AuditTargetType.USER,
                savedUser.getId().value(),
                beforeState,
                UserAuditData.from(savedUser)
        );

        return new ChangeNicknameResult(
                savedUser.getNickname()
        );
    }
}
