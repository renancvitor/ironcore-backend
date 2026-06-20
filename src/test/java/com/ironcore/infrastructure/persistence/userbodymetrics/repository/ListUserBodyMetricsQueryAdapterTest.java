package com.ironcore.infrastructure.persistence.userbodymetrics.repository;

import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.userbodymetrics.list.ListUserBodyMetricsItemResult;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.userbodymetrics.valueobject.UserBodyMetricsId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.userbodymetrics.entity.UserBodyMetricsEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.ironcore.infrastructure.persistence.userbodymetrics.UserBodyMetricsTestFactory.MEASURED_AT;
import static com.ironcore.infrastructure.persistence.userbodymetrics.UserBodyMetricsTestFactory.createUserBodyMetricsEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUserBodyMetricsQueryAdapterTest {

    @Mock
    private UserBodyMetricsJpaRepository userBodyMetricsJpaRepository;

    @InjectMocks
    private ListUserBodyMetricsQueryAdapter adapter;

    @Test
    void shouldReturnMappedPageAndForwardPagination() {
        PageRequest requestedPage = PageRequest.of(1, 2);
        Page<UserBodyMetricsEntity> entities = new PageImpl<>(
                List.of(createUserBodyMetricsEntity()),
                requestedPage,
                5
        );
        when(userBodyMetricsJpaRepository.findByUser_IdOrderByMeasuredAtDescIdDesc(any(), any()))
                .thenReturn(entities);

        PageResult<ListUserBodyMetricsItemResult> result = adapter.findByUserIdOrderByMeasuredAtDesc(
                new UserId(1L),
                new PageQuery(1, 2)
        );

        assertThat(result.content()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(new UserBodyMetricsId(1L));
            assertThat(item.measuredAt()).isEqualTo(MEASURED_AT);
            assertThat(item.weightKg()).isEqualTo(new BodyWeightKg(65.0));
            assertThat(item.heightCm()).isEqualTo(new BodyHeightCm(1.67));
            assertThat(item.notes()).isEqualTo("TEXT");
        });
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.last()).isFalse();

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userBodyMetricsJpaRepository)
                .findByUser_IdOrderByMeasuredAtDescIdDesc(org.mockito.ArgumentMatchers.eq(1L), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(2);
        assertThat(pageableCaptor.getValue().getSort().isUnsorted()).isTrue();
    }

    @Test
    void shouldReturnEmptyPageWhenUserDoesNotHaveBodyMetrics() {
        PageRequest requestedPage = PageRequest.of(0, 10);
        when(userBodyMetricsJpaRepository.findByUser_IdOrderByMeasuredAtDescIdDesc(99L, requestedPage))
                .thenReturn(Page.empty(requestedPage));

        PageResult<ListUserBodyMetricsItemResult> result = adapter.findByUserIdOrderByMeasuredAtDesc(
                new UserId(99L),
                new PageQuery(0, 10)
        );

        assertThat(result.content()).isEmpty();
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
        assertThat(result.last()).isTrue();
    }

    @Test
    void shouldWrapRepositoryFailure() {
        PageRequest requestedPage = PageRequest.of(0, 10);
        when(userBodyMetricsJpaRepository.findByUser_IdOrderByMeasuredAtDescIdDesc(1L, requestedPage))
                .thenThrow(new RuntimeException("database unavailable"));

        assertThatExceptionOfType(PersistenceException.class)
                .isThrownBy(() -> adapter.findByUserIdOrderByMeasuredAtDesc(
                        new UserId(1L),
                        new PageQuery(0, 10)
                ))
                .withMessage("Falha ao buscar histórico de métricas corporais do usuário.");
    }

    @Test
    void shouldWrapMappingFailure() {
        UserBodyMetricsEntity invalidEntity = createUserBodyMetricsEntity();
        invalidEntity.setId(null);
        PageRequest requestedPage = PageRequest.of(0, 10);
        when(userBodyMetricsJpaRepository.findByUser_IdOrderByMeasuredAtDescIdDesc(1L, requestedPage))
                .thenReturn(new PageImpl<>(List.of(invalidEntity), requestedPage, 1));

        assertThatExceptionOfType(DataMappingException.class)
                .isThrownBy(() -> adapter.findByUserIdOrderByMeasuredAtDesc(
                        new UserId(1L),
                        new PageQuery(0, 10)
                ))
                .withMessage("Falha ao converter histórico de métricas corporais do usuário.");
    }
}
