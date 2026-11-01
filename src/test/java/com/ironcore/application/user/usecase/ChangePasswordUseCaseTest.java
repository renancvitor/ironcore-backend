package com.ironcore.application.user.usecase;

import com.ironcore.application.user.service.UserPasswordChangeService;
import com.ironcore.application.user.usecase.changepassword.ChangePasswordCommand;
import com.ironcore.application.user.usecase.changepassword.ChangePasswordUseCase;
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

    @Mock
    private com.ironcore.application.auth.service.AuthSessionIssuer issuer;

    @InjectMocks
    private ChangePasswordUseCase changePasswordUseCase;

    @Test
    void shouldChangePassword() {
        ChangePasswordCommand command = command();

        var user = com.ironcore.domain.user.UserTestFactory.activeUser();
        org.mockito.Mockito.when(userPasswordChangeService.changePassword(command)).thenReturn(user);
        org.mockito.Mockito.when(issuer.issue(user)).thenReturn(com.ironcore.application.auth.LoginTestFactory.generatedAccessToken());
        org.assertj.core.api.Assertions.assertThat(changePasswordUseCase.execute(command)).isNotNull();
        verify(issuer).issue(user);

        verify(userPasswordChangeService).changePassword(command);
    }
}
