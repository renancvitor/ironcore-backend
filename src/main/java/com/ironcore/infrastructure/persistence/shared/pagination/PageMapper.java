package com.ironcore.infrastructure.persistence.shared.pagination;

import com.ironcore.application.shared.pagination.PageResult;
import org.springframework.data.domain.Page;

public final class PageMapper {

    private PageMapper() {
    }

    public static <T> PageResult<T> toPageResult(Page<T> page) {
        return new PageResult<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
