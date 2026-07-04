package com.ironcore.interfaces.rest.user;

import com.ironcore.application.user.usecase.changepassword.ChangePasswordCommand;
import com.ironcore.application.user.usecase.changepassword.ChangePasswordUseCase;
import com.ironcore.application.user.usecase.getauthenticateduser.GetAuthenticatedUserUseCase;
import com.ironcore.application.user.usecase.getauthenticateduser.UserProfileResult;
import com.ironcore.application.user.usecase.initialchangepassword.InitialChangePasswordCommand;
import com.ironcore.application.user.usecase.initialchangepassword.InitialChangePasswordUseCase;
import com.ironcore.application.user.usecase.update.ChangeNicknameCommand;
import com.ironcore.application.user.usecase.update.ChangeNicknameResult;
import com.ironcore.application.user.usecase.update.ChangeNicknameUseCase;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.user.dto.*;
import com.ironcore.interfaces.rest.user.mapper.UserChangePasswordMapper;
import com.ironcore.interfaces.rest.user.mapper.UserRestMapper;
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
    private final GetAuthenticatedUserUseCase getAuthenticatedUserUseCase;
    private final ChangeNicknameUseCase changeNicknameUseCase;

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

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getAuthenticatedUser(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        UserProfileResult result = getAuthenticatedUserUseCase.execute(authenticatedUser.userId());
        return ResponseEntity.ok(UserRestMapper.toResponse(result));
    }

    @PutMapping("/me/change-nickname")
    public ResponseEntity<ChangeNicknameResponse> changeNickname(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody ChangeNicknameRequest request
    ) {
        ChangeNicknameCommand command = UserRestMapper.toChangeNicknameCommand(authenticatedUser, request);
        ChangeNicknameResult result = changeNicknameUseCase.execute(command);
        ChangeNicknameResponse response = UserRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }
}
