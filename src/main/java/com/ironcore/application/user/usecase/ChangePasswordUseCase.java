package com.ironcore.application.user.usecase;

import com.ironcore.application.user.service.UserPasswordChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final UserPasswordChangeService userPasswordChangeService;

    @Transactional
    public void execute(ChangePasswordCommand command) {
        userPasswordChangeService.changePassword(command);
    }
}
