package com.ironcore.application.user.usecase.changepassword;

import com.ironcore.application.auth.port.SessionCredentials;
import com.ironcore.application.auth.service.AuthSessionIssuer;
import com.ironcore.application.user.service.UserPasswordChangeService;
import com.ironcore.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final UserPasswordChangeService userPasswordChangeService;
    private final AuthSessionIssuer authSessionIssuer;

    @Transactional
    public SessionCredentials execute(ChangePasswordCommand command) {
        User user = userPasswordChangeService.changePassword(command);

        return authSessionIssuer.issue(user);
    }
}
