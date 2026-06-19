package com.ironcore.interfaces.rest.userbodymetrics.mapper;

import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.delete.DeleteUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsResult;
import com.ironcore.domain.userbodymetrics.valueobject.*;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.userbodymetrics.dto.BodyCircumferencesRequest;
import com.ironcore.interfaces.rest.userbodymetrics.dto.BodyCircumferencesResponse;
import com.ironcore.interfaces.rest.userbodymetrics.dto.create.CreateUserBodyMetricsRequest;
import com.ironcore.interfaces.rest.userbodymetrics.dto.create.CreateUserBodyMetricsResponse;
import com.ironcore.interfaces.rest.userbodymetrics.dto.update.UpdateUserBodyMetricsRequest;
import com.ironcore.interfaces.rest.userbodymetrics.dto.update.UpdateUserBodyMetricsResponse;

public final class UserBodyMetricsRestMapper {

    private UserBodyMetricsRestMapper() {
    }

    public static CreateUserBodyMetricsCommand toCommand(
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

    public static UpdateUserBodyMetricsCommand toCommand(
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

    public static DeleteUserBodyMetricsCommand toDelete(AuthenticatedUser authenticatedUser, Long id) {
        return new DeleteUserBodyMetricsCommand(
                new UserBodyMetricsId(id),
                authenticatedUser.userId()
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
}
