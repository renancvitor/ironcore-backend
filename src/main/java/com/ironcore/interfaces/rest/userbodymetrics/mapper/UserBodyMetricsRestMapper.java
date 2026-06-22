package com.ironcore.interfaces.rest.userbodymetrics.mapper;

import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.delete.DeleteUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.get.GetUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.get.GetUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.latest.GetLatestUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.latest.GetLatestUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.list.ListUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.list.ListUserBodyMetricsItemResult;
import com.ironcore.application.userbodymetrics.list.ListUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsResult;
import com.ironcore.domain.userbodymetrics.valueobject.*;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.userbodymetrics.dto.BodyCircumferencesRequest;
import com.ironcore.interfaces.rest.userbodymetrics.dto.BodyCircumferencesResponse;
import com.ironcore.interfaces.rest.userbodymetrics.dto.create.CreateUserBodyMetricsRequest;
import com.ironcore.interfaces.rest.userbodymetrics.dto.create.CreateUserBodyMetricsResponse;
import com.ironcore.interfaces.rest.userbodymetrics.dto.get.GetUserBodyMetricsResponse;
import com.ironcore.interfaces.rest.userbodymetrics.dto.latest.GetLatestUserBodyMetricsResponse;
import com.ironcore.interfaces.rest.userbodymetrics.dto.list.ListUserBodyMetricsItemResponse;
import com.ironcore.interfaces.rest.userbodymetrics.dto.list.ListUserBodyMetricsResponse;
import com.ironcore.interfaces.rest.userbodymetrics.dto.update.UpdateUserBodyMetricsRequest;
import com.ironcore.interfaces.rest.userbodymetrics.dto.update.UpdateUserBodyMetricsResponse;

public final class UserBodyMetricsRestMapper {

    private UserBodyMetricsRestMapper() {
    }

    public static CreateUserBodyMetricsCommand toCreateCommand(
            AuthenticatedUser authenticatedUser,
            CreateUserBodyMetricsRequest request
    ) {
        return new CreateUserBodyMetricsCommand(
                authenticatedUser.userId(),
                new BodyWeightKg(request.weightKg()),
                new BodyHeightCm(request.heightCm()),
                toBodyCircumferences(request.circumferences()),
                request.notes()
        );
    }

    public static CreateUserBodyMetricsResponse toResponse(CreateUserBodyMetricsResult result) {
        return new CreateUserBodyMetricsResponse(
                result.id().value(),
                result.userId().value(),
                result.measuredAt(),
                result.weight().value(),
                result.height().value(),
                toBodyCircumferencesResponse(result.circumferences()),
                result.bmi().value(),
                result.bodyFatPercentage() == null ? null : result.bodyFatPercentage().value(),
                result.fatMassKg() == null ? null : result.fatMassKg().value(),
                result.leanMassKg() == null ? null : result.leanMassKg().value(),
                result.notes()
        );
    }

    public static UpdateUserBodyMetricsCommand toUpdateCommand(
            AuthenticatedUser authenticatedUser,
            Long bodyMetricsId,
            UpdateUserBodyMetricsRequest request
    ) {
        return new UpdateUserBodyMetricsCommand(
                new UserBodyMetricsId(bodyMetricsId),
                authenticatedUser.userId(),
                new BodyWeightKg(request.weightKg()),
                new BodyHeightCm(request.heightCm()),
                toBodyCircumferences(request.circumferences()),
                request.notes()
        );
    }

    public static UpdateUserBodyMetricsResponse toResponse(UpdateUserBodyMetricsResult result) {
        return new UpdateUserBodyMetricsResponse(
                result.id().value(),
                result.userId().value(),
                result.measuredAt(),
                result.weight().value(),
                result.height().value(),
                toBodyCircumferencesResponse(result.circumferences()),
                result.bmi().value(),
                result.bodyFatPercentage() == null ? null : result.bodyFatPercentage().value(),
                result.fatMassKg() == null ? null : result.fatMassKg().value(),
                result.leanMassKg() == null ? null : result.leanMassKg().value(),
                result.notes(),
                result.updatedAt()
        );
    }

    public static DeleteUserBodyMetricsCommand toDeleteCommand(AuthenticatedUser authenticatedUser, Long id) {
        return new DeleteUserBodyMetricsCommand(
                new UserBodyMetricsId(id),
                authenticatedUser.userId()
        );
    }

    public static ListUserBodyMetricsCommand toListCommand(
            AuthenticatedUser authenticatedUser,
            int page,
            int size
    ) {
        return new ListUserBodyMetricsCommand(
                authenticatedUser.userId(),
                page,
                size
        );
    }

    public static ListUserBodyMetricsResponse toResponse(ListUserBodyMetricsResult result) {
        PageResult<ListUserBodyMetricsItemResult> metrics = result.metrics();

        PageResult<ListUserBodyMetricsItemResponse> responsePage = new PageResult<>(
                metrics.content().stream()
                        .map(UserBodyMetricsRestMapper::toListItemResponse)
                        .toList(),
                metrics.page(),
                metrics.size(),
                metrics.totalElements(),
                metrics.totalPages(),
                metrics.last()
        );

        return new ListUserBodyMetricsResponse(responsePage);
    }

    public static GetUserBodyMetricsCommand toGetByIdCommand(AuthenticatedUser authenticatedUser, Long id) {
        return new GetUserBodyMetricsCommand(
                new UserBodyMetricsId(id),
                authenticatedUser.userId()
        );
    }

    public static GetUserBodyMetricsResponse toResponse(GetUserBodyMetricsResult result) {
        return new GetUserBodyMetricsResponse(
                result.id().value(),
                result.userId().value(),
                result.measuredAt(),
                result.weight().value(),
                result.height().value(),
                toBodyCircumferencesResponse(result.circumferences()),
                result.bmi().value(),
                result.bodyFatPercentage() == null ? null : result.bodyFatPercentage().value(),
                result.fatMassKg() == null ? null : result.fatMassKg().value(),
                result.leanMassKg() == null ? null : result.leanMassKg().value(),
                result.notes(),
                result.updatedAt()
        );
    }

    public static GetLatestUserBodyMetricsCommand toGetLatestCommand(AuthenticatedUser authenticatedUser) {
        return new  GetLatestUserBodyMetricsCommand(
                authenticatedUser.userId()
        );
    }

    public static GetLatestUserBodyMetricsResponse toResponse(GetLatestUserBodyMetricsResult result) {
        return new GetLatestUserBodyMetricsResponse(
                result.id().value(),
                result.userId().value(),
                result.measuredAt(),
                result.weight().value(),
                result.height().value(),
                toBodyCircumferencesResponse(result.circumferences()),
                result.bmi().value(),
                result.bodyFatPercentage() == null ? null : result.bodyFatPercentage().value(),
                result.fatMassKg() == null ? null : result.fatMassKg().value(),
                result.leanMassKg() == null ? null : result.leanMassKg().value(),
                result.notes(),
                result.updatedAt()
        );
    }

    private static BodyCircumferences toBodyCircumferences(BodyCircumferencesRequest request) {
        if (request == null) {
            return null;
        }

        return new BodyCircumferences(
                toBodyCircumferenceCm(request.neckCm()),
                toBodyCircumferenceCm(request.chestCm()),
                toBodyCircumferenceCm(request.shoulderCm()),
                toBodyCircumferenceCm(request.armCm()),
                toBodyCircumferenceCm(request.forearmCm()),
                toBodyCircumferenceCm(request.waistCm()),
                toBodyCircumferenceCm(request.hipCm()),
                toBodyCircumferenceCm(request.thighCm()),
                toBodyCircumferenceCm(request.calfCm())
        );
    }

    private static BodyCircumferenceCm toBodyCircumferenceCm(Double value) {
        return value == null ? null : new BodyCircumferenceCm(value);
    }

    private static BodyCircumferencesResponse toBodyCircumferencesResponse(BodyCircumferences circumferences) {
        if (circumferences == null) {
            return null;
        }

        return new BodyCircumferencesResponse(
                valueOf(circumferences.neck()),
                valueOf(circumferences.chest()),
                valueOf(circumferences.shoulder()),
                valueOf(circumferences.arm()),
                valueOf(circumferences.forearm()),
                valueOf(circumferences.waist()),
                valueOf(circumferences.hip()),
                valueOf(circumferences.thigh()),
                valueOf(circumferences.calf())
        );
    }

    private static Double valueOf(BodyCircumferenceCm circumferenceCm) {
        return circumferenceCm == null ? null : circumferenceCm.value();
    }

    private static ListUserBodyMetricsItemResponse toListItemResponse(
            ListUserBodyMetricsItemResult item
    ) {
        return new ListUserBodyMetricsItemResponse(
                item.id().value(),
                item.measuredAt(),
                item.weightKg().value(),
                item.heightCm().value(),
                item.notes()
        );
    }
}
