package com.ironcore.infrastructure.persistence.shared.pagination;

import com.ironcore.application.shared.pagination.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageMapperTest {

    @Test
    void shouldMapSpringPageToApplicationPageResult() {
        var page = new PageImpl<>(List.of("metric"), PageRequest.of(1, 2), 5);

        PageResult<String> result = PageMapper.toPageResult(page);

        assertThat(result.content()).containsExactly("metric");
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.last()).isFalse();
    }
}
