package com.ironcore.interfaces.rest.user;

import com.ironcore.application.user.usecase.ChangePasswordCommand;
import com.ironcore.application.user.usecase.ChangePasswordUseCase;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.user.dto.ChangePasswordRequest;
import com.ironcore.interfaces.rest.user.mapper.ChangePasswordMapper;
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

    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        ChangePasswordCommand command = ChangePasswordMapper.toCommand(authenticatedUser, request);
        changePasswordUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }
}
