package com.ironcore.application.userbodymetrics.port;

import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.userbodymetrics.list.ListUserBodyMetricsItemResult;
import com.ironcore.domain.user.valueobject.UserId;

public interface ListUserBodyMetricsQueryPort {

    PageResult<ListUserBodyMetricsItemResult> findByUserIdOrderByMeasuredAtDesc(
            UserId userId,
            PageQuery pageQuery
    );
}
