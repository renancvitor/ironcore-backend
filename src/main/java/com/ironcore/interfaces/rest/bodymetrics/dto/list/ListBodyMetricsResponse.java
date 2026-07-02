package com.ironcore.interfaces.rest.bodymetrics.dto.list;

import com.ironcore.application.shared.pagination.PageResult;

public record ListBodyMetricsResponse(
        PageResult<ListBodyMetricsItemResponse> metrics
) {
}
