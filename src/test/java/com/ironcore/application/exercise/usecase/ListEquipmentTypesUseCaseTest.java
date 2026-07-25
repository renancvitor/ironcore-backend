package com.ironcore.application.exercise.usecase;

import com.ironcore.application.exercise.catalog.port.ExerciseFilterCatalogQueryPort;
import com.ironcore.application.exercise.catalog.result.EquipmentTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.ListEquipmentTypesUseCase;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
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
class ListEquipmentTypesUseCaseTest {

    @Mock
    private ExerciseFilterCatalogQueryPort queryPort;

    @InjectMocks
    private ListEquipmentTypesUseCase listEquipmentTypesUseCase;

    @Test
    void shouldListActiveEquipmentTypes() {
        List<EquipmentTypeItemResult> expected = List.of(new EquipmentTypeItemResult(
                new EquipmentTypeId(1L),
                new EquipmentTypeCode("CABLE"),
                "Cabo"
        ));
        when(queryPort.findActiveEquipmentTypes()).thenReturn(expected);

        List<EquipmentTypeItemResult> result = listEquipmentTypesUseCase.execute();

        verify(queryPort).findActiveEquipmentTypes();
        assertThat(result).isEqualTo(expected);
    }
}
