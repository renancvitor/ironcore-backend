package com.ironcore.application.bodymetrics.port;

import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsItemResult;
import com.ironcore.domain.person.valueobject.PersonId;

public interface ListBodyMetricsQueryPort {

    PageResult<ListBodyMetricsItemResult> findByPersonIdOrderByMeasuredAtDesc(
            PersonId personId,
            PageQuery pageQuery
    );
}
