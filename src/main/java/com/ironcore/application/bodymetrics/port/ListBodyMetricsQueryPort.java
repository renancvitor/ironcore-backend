package com.ironcore.application.bodymetrics.port;

import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsItemResult;
import com.ironcore.domain.user.valueobject.UserId;

public interface ListBodyMetricsQueryPort {

    PageResult<ListBodyMetricsItemResult> findByUserIdOrderByMeasuredAtDesc(
            UserId userId,
            PageQuery pageQuery
    );
}
