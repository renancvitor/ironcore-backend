package com.ironcore.infrastructure.persistence.bodymetrics.repository;

import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsItemResult;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.bodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.bodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.bodymetrics.entity.BodyMetricsEntity;
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

import static com.ironcore.infrastructure.persistence.bodymetrics.BodyMetricsTestFactory.MEASURED_AT;
import static com.ironcore.infrastructure.persistence.bodymetrics.BodyMetricsTestFactory.createPersonBodyMetricsEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListBodyMetricsQueryAdapterTest {

    @Mock
    private BodyMetricsJpaRepository bodyMetricsJpaRepository;

    @InjectMocks
    private ListBodyMetricsQueryAdapter adapter;

    @Test
    void shouldReturnMappedPageAndForwardPagination() {
        PageRequest requestedPage = PageRequest.of(1, 2);
        Page<BodyMetricsEntity> entities = new PageImpl<>(
                List.of(createPersonBodyMetricsEntity()),
                requestedPage,
                5
        );
        when(bodyMetricsJpaRepository.findByPerson_IdOrderByMeasuredAtDescIdDesc(any(), any()))
                .thenReturn(entities);

        PageResult<ListBodyMetricsItemResult> result = adapter.findByPersonIdOrderByMeasuredAtDesc(
                new PersonId(1L),
                new PageQuery(1, 2)
        );

        assertThat(result.content()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(new BodyMetricsId(1L));
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
        verify(bodyMetricsJpaRepository)
                .findByPerson_IdOrderByMeasuredAtDescIdDesc(org.mockito.ArgumentMatchers.eq(1L), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(2);
        assertThat(pageableCaptor.getValue().getSort().isUnsorted()).isTrue();
    }

    @Test
    void shouldReturnEmptyPageWhenPersonDoesNotHaveBodyMetrics() {
        PageRequest requestedPage = PageRequest.of(0, 10);
        when(bodyMetricsJpaRepository.findByPerson_IdOrderByMeasuredAtDescIdDesc(99L, requestedPage))
                .thenReturn(Page.empty(requestedPage));

        PageResult<ListBodyMetricsItemResult> result = adapter.findByPersonIdOrderByMeasuredAtDesc(
                new PersonId(99L),
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
        when(bodyMetricsJpaRepository.findByPerson_IdOrderByMeasuredAtDescIdDesc(1L, requestedPage))
                .thenThrow(new RuntimeException("database unavailable"));

        assertThatExceptionOfType(PersistenceException.class)
                .isThrownBy(() -> adapter.findByPersonIdOrderByMeasuredAtDesc(
                        new PersonId(1L),
                        new PageQuery(0, 10)
                ))
                .withMessage("Falha ao buscar histórico de métricas corporais da pessoa.");
    }

    @Test
    void shouldWrapMappingFailure() {
        BodyMetricsEntity invalidEntity = createPersonBodyMetricsEntity();
        invalidEntity.setId(null);
        PageRequest requestedPage = PageRequest.of(0, 10);
        when(bodyMetricsJpaRepository.findByPerson_IdOrderByMeasuredAtDescIdDesc(1L, requestedPage))
                .thenReturn(new PageImpl<>(List.of(invalidEntity), requestedPage, 1));

        assertThatExceptionOfType(DataMappingException.class)
                .isThrownBy(() -> adapter.findByPersonIdOrderByMeasuredAtDesc(
                        new PersonId(1L),
                        new PageQuery(0, 10)
                ))
                .withMessage("Falha ao converter histórico de métricas corporais da pessoa.");
    }
}
