package com.ironcore.interfaces.rest.bodymetrics;

import com.ironcore.application.bodymetrics.create.CreateBodyMetricsCommand;
import com.ironcore.application.bodymetrics.create.CreateBodyMetricsResult;
import com.ironcore.application.bodymetrics.create.CreateBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.delete.DeleteBodyMetricsCommand;
import com.ironcore.application.bodymetrics.delete.DeleteBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.get.GetBodyMetricsCommand;
import com.ironcore.application.bodymetrics.get.GetBodyMetricsResult;
import com.ironcore.application.bodymetrics.get.GetBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.latest.GetLatestBodyMetricsCommand;
import com.ironcore.application.bodymetrics.latest.GetLatestBodyMetricsResult;
import com.ironcore.application.bodymetrics.latest.GetLatestBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsCommand;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsResult;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.progress.*;
import com.ironcore.application.bodymetrics.update.UpdateBodyMetricsCommand;
import com.ironcore.application.bodymetrics.update.UpdateBodyMetricsResult;
import com.ironcore.application.bodymetrics.update.UpdateBodyMetricsUseCase;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.bodymetrics.dto.create.CreateBodyMetricsRequest;
import com.ironcore.interfaces.rest.bodymetrics.dto.create.CreateBodyMetricsResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.get.GetBodyMetricsResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.latest.GetLatestBodyMetricsResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.list.ListBodyMetricsResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.progress.BodyMetricsProgressChangesRequest;
import com.ironcore.interfaces.rest.bodymetrics.dto.progress.BodyMetricsProgressChartRequest;
import com.ironcore.interfaces.rest.bodymetrics.dto.progress.BodyMetricsProgressChangesResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.progress.BodyMetricsProgressChartResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.update.UpdateBodyMetricsRequest;
import com.ironcore.interfaces.rest.bodymetrics.dto.update.UpdateBodyMetricsResponse;
import com.ironcore.interfaces.rest.bodymetrics.mapper.BodyMetricsRestMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Medidas corporais")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/users/me/body-metrics")
public class BodyMetricsController {

    private final CreateBodyMetricsUseCase createBodyMetricsUseCase;
    private final UpdateBodyMetricsUseCase updateBodyMetricsUseCase;
    private final DeleteBodyMetricsUseCase deleteBodyMetricsUseCase;
    private final ListBodyMetricsUseCase listBodyMetricsUseCase;
    private final GetBodyMetricsUseCase getBodyMetricsUseCase;
    private final GetLatestBodyMetricsUseCase getLatestBodyMetricsUseCase;
    private final GetBodyMetricsProgressChartUseCase getBodyMetricsProgressChartUseCase;
    private final GetBodyMetricsProgressChangesUseCase getBodyMetricsProgressChangesUseCase;

    @PostMapping
    public ResponseEntity<CreateBodyMetricsResponse> create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateBodyMetricsRequest request
    ) {
        CreateBodyMetricsCommand command = BodyMetricsRestMapper.toCreateCommand(authenticatedUser, request);
        CreateBodyMetricsResult result = createBodyMetricsUseCase.execute(command);
        CreateBodyMetricsResponse response = BodyMetricsRestMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateBodyMetricsResponse> update(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateBodyMetricsRequest request
    ) {
        UpdateBodyMetricsCommand command = BodyMetricsRestMapper.toUpdateCommand(authenticatedUser, id, request);
        UpdateBodyMetricsResult result = updateBodyMetricsUseCase.execute(command);
        UpdateBodyMetricsResponse response = BodyMetricsRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id
    ) {
        DeleteBodyMetricsCommand command = BodyMetricsRestMapper.toDeleteCommand(authenticatedUser, id);
        deleteBodyMetricsUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ListBodyMetricsResponse> list(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(name = "page", defaultValue = "0")
            @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20")
            @Min(1) @Max(100) int size
    ) {
        ListBodyMetricsCommand command = BodyMetricsRestMapper.toListCommand(
                authenticatedUser,
                page,
                size
        );
        ListBodyMetricsResult result = listBodyMetricsUseCase.execute(command);
        ListBodyMetricsResponse response = BodyMetricsRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetBodyMetricsResponse> getById(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id
    ) {
        GetBodyMetricsCommand command = BodyMetricsRestMapper.toGetByIdCommand(authenticatedUser, id);
        GetBodyMetricsResult result = getBodyMetricsUseCase.execute(command);
        GetBodyMetricsResponse response = BodyMetricsRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest")
    public ResponseEntity<GetLatestBodyMetricsResponse> getLatest(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        GetLatestBodyMetricsCommand command = BodyMetricsRestMapper.toGetLatestCommand(authenticatedUser);
        GetLatestBodyMetricsResult result = getLatestBodyMetricsUseCase.execute(command);
        GetLatestBodyMetricsResponse response = BodyMetricsRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress/body-composition")
    public ResponseEntity<BodyMetricsProgressChartResponse> getBodyComposition(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @ModelAttribute BodyMetricsProgressChartRequest request
    ) {
        BodyMetricsProgressChartCommand command = BodyMetricsRestMapper.toProgressChartCommand(
                authenticatedUser,
                BodyMetricsProgressChartType.BODY_COMPOSITION,
                request
        );
        GetBodyMetricsProgressChartResult result = getBodyMetricsProgressChartUseCase.execute(command);
        BodyMetricsProgressChartResponse response = BodyMetricsRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress/circumferences")
    public ResponseEntity<BodyMetricsProgressChartResponse> getCircumferences(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @ModelAttribute BodyMetricsProgressChartRequest request
    ) {
        BodyMetricsProgressChartCommand command = BodyMetricsRestMapper.toProgressChartCommand(
                authenticatedUser,
                BodyMetricsProgressChartType.CIRCUMFERENCES,
                request
        );
        GetBodyMetricsProgressChartResult result = getBodyMetricsProgressChartUseCase.execute(command);
        BodyMetricsProgressChartResponse response = BodyMetricsRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress/body-fat")
    public ResponseEntity<BodyMetricsProgressChartResponse> getBodyFat(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @ModelAttribute BodyMetricsProgressChartRequest request
    ) {
        BodyMetricsProgressChartCommand command = BodyMetricsRestMapper.toProgressChartCommand(
                authenticatedUser,
                BodyMetricsProgressChartType.BODY_FAT,
                request
        );
        GetBodyMetricsProgressChartResult result = getBodyMetricsProgressChartUseCase.execute(command);
        BodyMetricsProgressChartResponse response = BodyMetricsRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress/changes")
    public ResponseEntity<BodyMetricsProgressChangesResponse> getChanges(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @ModelAttribute BodyMetricsProgressChangesRequest request
    ) {
        BodyMetricsProgressChangesCommand command = BodyMetricsRestMapper.toProgressChangesCommand(
                authenticatedUser,
                request
        );
        GetBodyMetricsProgressChangesResult result = getBodyMetricsProgressChangesUseCase.execute(command);
        BodyMetricsProgressChangesResponse response = BodyMetricsRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }
}
