package com.ironcore.interfaces.rest.auth;

import com.ironcore.application.auth.usecase.LoginCommand;
import com.ironcore.application.auth.usecase.LoginResult;
import com.ironcore.application.auth.usecase.LoginUseCase;
import com.ironcore.interfaces.rest.auth.dto.LoginRequest;
import com.ironcore.interfaces.rest.auth.dto.LoginResponse;
import com.ironcore.interfaces.rest.auth.mapper.AuthRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = AuthRestMapper.toCommand(request);
        LoginResult result = loginUseCase.execute(command);
        LoginResponse response = AuthRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }
}
