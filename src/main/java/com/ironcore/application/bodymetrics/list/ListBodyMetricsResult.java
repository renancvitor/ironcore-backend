package com.ironcore.application.bodymetrics.list;

import com.ironcore.application.shared.pagination.PageResult;

public record ListBodyMetricsResult(
        PageResult<ListBodyMetricsItemResult> metrics
) {
}
