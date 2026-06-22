package com.ironcore.interfaces.rest.userbodymetrics;

import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.delete.DeleteUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.delete.DeleteUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.get.GetUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.get.GetUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.get.GetUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.latest.GetLatestUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.latest.GetLatestUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.latest.GetLatestUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.list.ListUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.list.ListUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.list.ListUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsUseCase;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.userbodymetrics.dto.create.CreateUserBodyMetricsRequest;
import com.ironcore.interfaces.rest.userbodymetrics.dto.create.CreateUserBodyMetricsResponse;
import com.ironcore.interfaces.rest.userbodymetrics.dto.get.GetUserBodyMetricsResponse;
import com.ironcore.interfaces.rest.userbodymetrics.dto.latest.GetLatestUserBodyMetricsResponse;
import com.ironcore.interfaces.rest.userbodymetrics.dto.list.ListUserBodyMetricsResponse;
import com.ironcore.interfaces.rest.userbodymetrics.dto.update.UpdateUserBodyMetricsRequest;
import com.ironcore.interfaces.rest.userbodymetrics.dto.update.UpdateUserBodyMetricsResponse;
import com.ironcore.interfaces.rest.userbodymetrics.mapper.UserBodyMetricsRestMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/users/me/body-metrics")
public class UserBodyMetricsController {

    private final CreateUserBodyMetricsUseCase createUserBodyMetricsUseCase;
    private final UpdateUserBodyMetricsUseCase updateUserBodyMetricsUseCase;
    private final DeleteUserBodyMetricsUseCase deleteUserBodyMetricsUseCase;
    private final ListUserBodyMetricsUseCase listUserBodyMetricsUseCase;
    private final GetUserBodyMetricsUseCase getUserBodyMetricsUseCase;
    private final GetLatestUserBodyMetricsUseCase getLatestUserBodyMetricsUseCase;

    @PostMapping
    public ResponseEntity<CreateUserBodyMetricsResponse> create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateUserBodyMetricsRequest request
    ) {
        CreateUserBodyMetricsCommand command = UserBodyMetricsRestMapper.toCreateCommand(authenticatedUser, request);
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
        UpdateUserBodyMetricsCommand command = UserBodyMetricsRestMapper.toUpdateCommand(authenticatedUser, id, request);
        UpdateUserBodyMetricsResult result = updateUserBodyMetricsUseCase.execute(command);
        UpdateUserBodyMetricsResponse response = UserBodyMetricsRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id
    ) {
        DeleteUserBodyMetricsCommand command = UserBodyMetricsRestMapper.toDeleteCommand(authenticatedUser, id);
        deleteUserBodyMetricsUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ListUserBodyMetricsResponse> list(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(name = "page", defaultValue = "0")
            @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20")
            @Min(1) @Max(100) int size
    ) {
        ListUserBodyMetricsCommand command = UserBodyMetricsRestMapper.toListCommand(
                authenticatedUser,
                page,
                size
        );
        ListUserBodyMetricsResult result = listUserBodyMetricsUseCase.execute(command);
        ListUserBodyMetricsResponse response = UserBodyMetricsRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetUserBodyMetricsResponse> getById(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id
    ) {
        GetUserBodyMetricsCommand command = UserBodyMetricsRestMapper.toGetByIdCommand(authenticatedUser, id);
        GetUserBodyMetricsResult result = getUserBodyMetricsUseCase.execute(command);
        GetUserBodyMetricsResponse response = UserBodyMetricsRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest")
    public ResponseEntity<GetLatestUserBodyMetricsResponse> getLatest(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        GetLatestUserBodyMetricsCommand command = UserBodyMetricsRestMapper.toGetLatestCommand(authenticatedUser);
        GetLatestUserBodyMetricsResult result = getLatestUserBodyMetricsUseCase.execute(command);
        GetLatestUserBodyMetricsResponse response = UserBodyMetricsRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }
}
