package com.ironcore.interfaces.rest.userbodymetrics.dto.list;

import com.ironcore.application.shared.pagination.PageResult;

public record ListUserBodyMetricsResponse(
        PageResult<ListUserBodyMetricsItemResponse> metrics
) {
}
