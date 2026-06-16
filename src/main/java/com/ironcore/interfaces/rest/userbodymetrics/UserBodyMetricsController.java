package com.ironcore.interfaces.rest.userbodymetrics;

import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.CreateUserBodyMetricsUseCase;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.userbodymetrics.dto.CreateUserBodyMetricsRequest;
import com.ironcore.interfaces.rest.userbodymetrics.dto.CreateUserBodyMetricsResponse;
import com.ironcore.interfaces.rest.userbodymetrics.mapper.UserBodyMetricsRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserBodyMetricsController {

    private final CreateUserBodyMetricsUseCase createUserBodyMetricsUseCase;

    @PostMapping("/me/body-metrics")
    public ResponseEntity<CreateUserBodyMetricsResponse> create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateUserBodyMetricsRequest request
    ) {
        CreateUserBodyMetricsCommand command = UserBodyMetricsRestMapper.toCommand(authenticatedUser, request);
        CreateUserBodyMetricsResult result = createUserBodyMetricsUseCase.execute(command);
        CreateUserBodyMetricsResponse response = UserBodyMetricsRestMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
