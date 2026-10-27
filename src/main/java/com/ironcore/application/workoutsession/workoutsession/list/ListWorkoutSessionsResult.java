package com.ironcore.application.workoutsession.workoutsession.list;

import com.ironcore.application.shared.pagination.PageResult;

public record ListWorkoutSessionsResult(
        PageResult<ListWorkoutSessionsItemResult> sessions
) {
}
