package com.ironcore.application.userbodymetrics.list;

import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.domain.user.valueobject.UserId;

public interface ListUserBodyMetricsQueryPort {

    PageResult<ListUserBodyMetricsItemResult> findByUserIdOrderByMeasuredAtDesc(
            UserId userId,
            PageQuery pageQuery
    );
}
