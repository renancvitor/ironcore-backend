package com.ironcore.application.exercise.usecase;

import com.ironcore.application.exercise.catalog.port.ExerciseFilterCatalogQueryPort;
import com.ironcore.application.exercise.catalog.usecase.ActivityTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.ListActivityTypesUseCase;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListActivityTypesUseCaseTest {

    @Mock
    private ExerciseFilterCatalogQueryPort queryPort;

    @InjectMocks
    private ListActivityTypesUseCase listActivityTypesUseCase;

    @Test
    void shouldListActiveActivityTypes() {
        List<ActivityTypeItemResult> expected = List.of(new ActivityTypeItemResult(
                new ActivityTypeId(1L),
                new ActivityTypeCode("STRENGTH"),
                "Força"
        ));
        when(queryPort.findActiveActivityTypes()).thenReturn(expected);

        List<ActivityTypeItemResult> result = listActivityTypesUseCase.execute();

        verify(queryPort).findActiveActivityTypes();
        assertThat(result).isEqualTo(expected);
    }
}
