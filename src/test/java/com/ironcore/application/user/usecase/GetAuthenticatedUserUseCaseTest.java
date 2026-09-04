package com.ironcore.application.user.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.user.usecase.getauthenticateduser.GetAuthenticatedUserUseCase;
import com.ironcore.application.user.usecase.getauthenticateduser.UserProfileResult;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAuthenticatedUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetAuthenticatedUserUseCase getAuthenticatedUserUseCase;

    @Nested
    class Success {

        @Test
        void shouldReturnAuthenticatedUser() {
            User user = activeUser();

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            UserProfileResult result = getAuthenticatedUserUseCase.execute(user.getId());

            assertThat(result.userId()).isEqualTo(user.getId());
            assertThat(result.email()).isEqualTo(user.getEmail());
            assertThat(result.nickname()).isEqualTo(user.getNickname());
            assertThat(result.mustChangePassword()).isEqualTo(user.mustChangePassword());
            verify(userRepository).findById(user.getId());
        }
    }

    @Nested
    class Failure {

        @Test
        void shouldFailWhenUserNotFound() {
            User user = activeUser();

            when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> getAuthenticatedUserUseCase.execute(user.getId()))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(user.getId());
        }

        @Test
        void shouldFailWhenUserIsNotActive() {
            User user = inactiveUser();

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> getAuthenticatedUserUseCase.execute(user.getId()))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(user.getId());
        }
    }
}
