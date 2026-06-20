package com.ironcore.application.userbodymetrics.list;

import com.ironcore.application.shared.pagination.PageResult;

public record ListUserBodyMetricsResult(
        PageResult<ListUserBodyMetricsItemResult> metrics
) {
}
