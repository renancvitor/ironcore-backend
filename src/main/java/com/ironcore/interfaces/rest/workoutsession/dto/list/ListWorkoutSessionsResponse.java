package com.ironcore.interfaces.rest.workoutsession.dto.list;

import com.ironcore.application.shared.pagination.PageResult;

public record ListWorkoutSessionsResponse(
        PageResult<ListWorkoutSessionsItemResponse> sessions
) {
}
