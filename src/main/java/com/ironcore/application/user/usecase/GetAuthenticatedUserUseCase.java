package com.ironcore.application.user.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAuthenticatedUserUseCase {

    private final UserRepository userRepository;

    public UserProfileResult execute(UserId userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new OperationNotAllowedException("Usuário inativo.");
        }

        return new UserProfileResult(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }
}
