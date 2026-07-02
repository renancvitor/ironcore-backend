package com.ironcore.interfaces.rest.bodymetrics.mapper;

import com.ironcore.application.bodymetrics.progress.*;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.bodymetrics.create.CreateBodyMetricsCommand;
import com.ironcore.application.bodymetrics.create.CreateBodyMetricsResult;
import com.ironcore.application.bodymetrics.delete.DeleteBodyMetricsCommand;
import com.ironcore.application.bodymetrics.get.GetBodyMetricsCommand;
import com.ironcore.application.bodymetrics.get.GetBodyMetricsResult;
import com.ironcore.application.bodymetrics.latest.GetLatestBodyMetricsCommand;
import com.ironcore.application.bodymetrics.latest.GetLatestBodyMetricsResult;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsCommand;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsItemResult;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsResult;
import com.ironcore.application.bodymetrics.update.UpdateBodyMetricsCommand;
import com.ironcore.application.bodymetrics.update.UpdateBodyMetricsResult;
import com.ironcore.domain.bodymetrics.valueobject.*;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.bodymetrics.dto.BodyCircumferencesRequest;
import com.ironcore.interfaces.rest.bodymetrics.dto.BodyCircumferencesResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.create.CreateBodyMetricsRequest;
import com.ironcore.interfaces.rest.bodymetrics.dto.create.CreateBodyMetricsResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.get.GetBodyMetricsResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.latest.GetLatestBodyMetricsResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.list.ListBodyMetricsItemResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.list.ListBodyMetricsResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.progress.*;
import com.ironcore.interfaces.rest.bodymetrics.dto.update.UpdateBodyMetricsRequest;
import com.ironcore.interfaces.rest.bodymetrics.dto.update.UpdateBodyMetricsResponse;

public final class BodyMetricsRestMapper {

    private BodyMetricsRestMapper() {
    }

    public static CreateBodyMetricsCommand toCreateCommand(
            AuthenticatedUser authenticatedUser,
            CreateBodyMetricsRequest request
    ) {
        return new CreateBodyMetricsCommand(
                authenticatedUser.userId(),
                new BodyWeightKg(request.weightKg()),
                new BodyHeightCm(request.heightCm()),
                toBodyCircumferences(request.circumferences()),
                request.notes()
        );
    }

    public static CreateBodyMetricsResponse toResponse(CreateBodyMetricsResult result) {
        return new CreateBodyMetricsResponse(
                result.id().value(),
                result.personId().value(),
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

    public static UpdateBodyMetricsCommand toUpdateCommand(
            AuthenticatedUser authenticatedUser,
            Long bodyMetricsId,
            UpdateBodyMetricsRequest request
    ) {
        return new UpdateBodyMetricsCommand(
                new BodyMetricsId(bodyMetricsId),
                authenticatedUser.userId(),
                new BodyWeightKg(request.weightKg()),
                new BodyHeightCm(request.heightCm()),
                toBodyCircumferences(request.circumferences()),
                request.notes()
        );
    }

    public static UpdateBodyMetricsResponse toResponse(UpdateBodyMetricsResult result) {
        return new UpdateBodyMetricsResponse(
                result.id().value(),
                result.personId().value(),
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

    public static DeleteBodyMetricsCommand toDeleteCommand(AuthenticatedUser authenticatedUser, Long id) {
        return new DeleteBodyMetricsCommand(
                new BodyMetricsId(id),
                authenticatedUser.userId()
        );
    }

    public static ListBodyMetricsCommand toListCommand(
            AuthenticatedUser authenticatedUser,
            int page,
            int size
    ) {
        return new ListBodyMetricsCommand(
                authenticatedUser.userId(),
                page,
                size
        );
    }

    public static ListBodyMetricsResponse toResponse(ListBodyMetricsResult result) {
        PageResult<ListBodyMetricsItemResult> metrics = result.metrics();

        PageResult<ListBodyMetricsItemResponse> responsePage = new PageResult<>(
                metrics.content().stream()
                        .map(BodyMetricsRestMapper::toListItemResponse)
                        .toList(),
                metrics.page(),
                metrics.size(),
                metrics.totalElements(),
                metrics.totalPages(),
                metrics.last()
        );

        return new ListBodyMetricsResponse(responsePage);
    }

    public static GetBodyMetricsCommand toGetByIdCommand(AuthenticatedUser authenticatedUser, Long id) {
        return new GetBodyMetricsCommand(
                new BodyMetricsId(id),
                authenticatedUser.userId()
        );
    }

    public static GetBodyMetricsResponse toResponse(GetBodyMetricsResult result) {
        return new GetBodyMetricsResponse(
                result.id().value(),
                result.personId().value(),
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

    public static GetLatestBodyMetricsCommand toGetLatestCommand(AuthenticatedUser authenticatedUser) {
        return new GetLatestBodyMetricsCommand(
                authenticatedUser.userId()
        );
    }

    public static GetLatestBodyMetricsResponse toResponse(GetLatestBodyMetricsResult result) {
        return new GetLatestBodyMetricsResponse(
                result.id().value(),
                result.personId().value(),
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

    public static BodyMetricsProgressChartCommand toProgressChartCommand(
            AuthenticatedUser authenticatedUser,
            BodyMetricsProgressChartType chartType,
            BodyMetricsProgressChartRequest request
    ) {
        return new BodyMetricsProgressChartCommand(
                authenticatedUser.userId(),
                chartType,
                request.startDate(),
                request.endDate()
        );
    }

    public static BodyMetricsProgressChartResponse toResponse(GetBodyMetricsProgressChartResult result) {
        return new BodyMetricsProgressChartResponse(
                result.startDate(),
                result.endDate(),
                result.chartType(),
                result.series().stream()
                        .map(BodyMetricsRestMapper::toProgressSeriesResponse)
                        .toList()
        );
    }

    public static BodyMetricsProgressChangesCommand toProgressChangesCommand(
            AuthenticatedUser authenticatedUser,
            BodyMetricsProgressChangesRequest request
    ) {
        return new BodyMetricsProgressChangesCommand(
                authenticatedUser.userId(),
                request.startDate(),
                request.endDate()
        );
    }

    public static BodyMetricsProgressChangesResponse toResponse(GetBodyMetricsProgressChangesResult result) {
        return new BodyMetricsProgressChangesResponse(
                result.startDate(),
                result.endDate(),
                result.changes().stream()
                        .map(BodyMetricsRestMapper::toProgressChangeResponse)
                        .toList()
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

    private static ListBodyMetricsItemResponse toListItemResponse(
            ListBodyMetricsItemResult item
    ) {
        return new ListBodyMetricsItemResponse(
                item.id().value(),
                item.measuredAt(),
                item.weightKg().value(),
                item.heightCm().value(),
                item.notes()
        );
    }

    private static BodyMetricsProgressSeriesResponse toProgressSeriesResponse(
            BodyMetricsProgressSeriesResult result
    ) {
        return new BodyMetricsProgressSeriesResponse(
                result.metric(),
                result.label(),
                result.unit(),
                result.points().stream()
                        .map(BodyMetricsRestMapper::toProgressPointResponse)
                        .toList()
        );
    }

    private static BodyMetricsProgressPointResponse toProgressPointResponse(
            BodyMetricsProgressPointResult result
    ) {
        return new BodyMetricsProgressPointResponse(
                result.period(),
                result.value()
        );
    }

    private static BodyMetricsProgressChangeResponse toProgressChangeResponse(
            BodyMetricsProgressChangeResult result
    ) {
        return new BodyMetricsProgressChangeResponse(
                result.metric(),
                result.label(),
                result.unit(),
                result.firstDate(),
                result.firstValue(),
                result.lastDate(),
                result.lastValue(),
                result.absoluteChange(),
                result.percentageChange()
        );
    }
}
