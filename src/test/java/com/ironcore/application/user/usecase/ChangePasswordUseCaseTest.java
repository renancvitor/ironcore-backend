package com.ironcore.application.user.usecase;

import com.ironcore.application.user.service.UserPasswordChangeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ironcore.application.user.ChangePasswordTestFactory.command;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChangePasswordUseCaseTest {

    @Mock
    private UserPasswordChangeService userPasswordChangeService;

    @InjectMocks
    private ChangePasswordUseCase changePasswordUseCase;

    @Test
    void shouldChangePassword() {
        ChangePasswordCommand command = command();

        changePasswordUseCase.execute(command);

        verify(userPasswordChangeService).changePassword(command);
    }
}
