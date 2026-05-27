package com.ironcore.interfaces.rest.user;

import com.ironcore.application.user.usecase.ChangePasswordCommand;
import com.ironcore.application.user.usecase.ChangePasswordUseCase;
import com.ironcore.application.user.usecase.InitialChangePasswordCommand;
import com.ironcore.application.user.usecase.InitialChangePasswordUseCase;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.user.dto.ChangePasswordRequest;
import com.ironcore.interfaces.rest.user.dto.InitialChangePasswordRequest;
import com.ironcore.interfaces.rest.user.mapper.UserChangePasswordMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final ChangePasswordUseCase changePasswordUseCase;
    private final InitialChangePasswordUseCase initialChangePasswordUseCase;

    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        ChangePasswordCommand command = UserChangePasswordMapper.toChangePasswordCommand(authenticatedUser, request);
        changePasswordUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-initial-password")
    public ResponseEntity<Void>  changeInitialPassword(
            @RequestBody @Valid InitialChangePasswordRequest request
    ) {
        InitialChangePasswordCommand command = UserChangePasswordMapper.toInitialChangePasswordCommand(request);
        initialChangePasswordUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }
}
