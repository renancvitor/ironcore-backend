package com.ironcore.infrastructure.persistence.activitytype.repository;

import com.ironcore.domain.activitytype.model.ActivityType;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.ironcore.infrastructure.persistence.activitytype.ActivityTypeEntityTestFactory.activityTypeEntity;
import static com.ironcore.infrastructure.persistence.activitytype.ActivityTypeEntityTestFactory.invalidActivityTypeEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityTypeAdapterTest {

    @Mock
    private ActivityTypeJpaRepository activityTypeJpaRepository;

    @InjectMocks
    private ActivityTypeAdapter adapter;

    @Nested
    class FindById {

        @Test
        void shouldFindActivityTypeById() {
            when(activityTypeJpaRepository.findById(1L)).thenReturn(Optional.of(activityTypeEntity()));

            Optional<ActivityType> result = adapter.findById(new ActivityTypeId(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new ActivityTypeId(1L));
            assertThat(result.get().getCode()).isEqualTo(new ActivityTypeCode("STRENGTH"));
        }

        @Test
        void shouldReturnEmptyWhenIdDoesNotExist() {
            when(activityTypeJpaRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<ActivityType> result = adapter.findById(new ActivityTypeId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(activityTypeJpaRepository.findById(1L)).thenThrow(new RuntimeException("database unavailable"));
            ActivityTypeId activityTypeId = new ActivityTypeId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findById(activityTypeId))
                    .withMessage("Falha ao buscar activity type por id.");
        }

        @Test
        void shouldWrapMappingFailure() {
            when(activityTypeJpaRepository.findById(1L)).thenReturn(Optional.of(invalidActivityTypeEntity()));
            ActivityTypeId activityTypeId = new ActivityTypeId(1L);

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.findById(activityTypeId))
                    .withMessage("Falha ao converter activity type por id para domínio.");
        }
    }

    @Nested
    class FindByCode {

        @Test
        void shouldFindActivityTypeByCode() {
            when(activityTypeJpaRepository.findByCode("STRENGTH")).thenReturn(Optional.of(activityTypeEntity()));

            Optional<ActivityType> result = adapter.findByCode(new ActivityTypeCode(" strength "));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new ActivityTypeId(1L));
            assertThat(result.get().getCode()).isEqualTo(new ActivityTypeCode("STRENGTH"));
        }

        @Test
        void shouldReturnEmptyWhenCodeDoesNotExist() {
            when(activityTypeJpaRepository.findByCode("MISSING")).thenReturn(Optional.empty());

            Optional<ActivityType> result = adapter.findByCode(new ActivityTypeCode("missing"));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(activityTypeJpaRepository.findByCode("STRENGTH"))
                    .thenThrow(new RuntimeException("database unavailable"));
            ActivityTypeCode code = new ActivityTypeCode("STRENGTH");

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByCode(code))
                    .withMessage("Falha ao buscar activity type por code.");
        }
    }

}
