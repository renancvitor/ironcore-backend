package com.ironcore.interfaces.rest.userbodymetrics;

import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.delete.DeleteUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.delete.DeleteUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsUseCase;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.userbodymetrics.dto.create.CreateUserBodyMetricsRequest;
import com.ironcore.interfaces.rest.userbodymetrics.dto.create.CreateUserBodyMetricsResponse;
import com.ironcore.interfaces.rest.userbodymetrics.dto.update.UpdateUserBodyMetricsRequest;
import com.ironcore.interfaces.rest.userbodymetrics.dto.update.UpdateUserBodyMetricsResponse;
import com.ironcore.interfaces.rest.userbodymetrics.mapper.UserBodyMetricsRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/body-metrics")
public class UserBodyMetricsController {

    private final CreateUserBodyMetricsUseCase createUserBodyMetricsUseCase;
    private final UpdateUserBodyMetricsUseCase updateUserBodyMetricsUseCase;
    private final DeleteUserBodyMetricsUseCase deleteUserBodyMetricsUseCase;

    @PostMapping
    public ResponseEntity<CreateUserBodyMetricsResponse> create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateUserBodyMetricsRequest request
    ) {
        CreateUserBodyMetricsCommand command = UserBodyMetricsRestMapper.toCommand(authenticatedUser, request);
        CreateUserBodyMetricsResult result = createUserBodyMetricsUseCase.execute(command);
        CreateUserBodyMetricsResponse response = UserBodyMetricsRestMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateUserBodyMetricsResponse> update(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserBodyMetricsRequest request
    ) {
        UpdateUserBodyMetricsCommand command = UserBodyMetricsRestMapper.toCommand(authenticatedUser, id, request);
        UpdateUserBodyMetricsResult result = updateUserBodyMetricsUseCase.execute(command);
        UpdateUserBodyMetricsResponse response = UserBodyMetricsRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id
    ) {
        DeleteUserBodyMetricsCommand command = UserBodyMetricsRestMapper.toDelete(authenticatedUser, id);
        deleteUserBodyMetricsUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }
}
