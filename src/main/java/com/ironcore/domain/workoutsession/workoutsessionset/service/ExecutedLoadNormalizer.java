package com.ironcore.domain.workoutsession.workoutsessionset.service;

import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;

import java.math.BigDecimal;

public final class ExecutedLoadNormalizer {

    private ExecutedLoadNormalizer() {
    }

    public static BigDecimal normalize(BigDecimal load, ActualLoadMode mode) {
        if (load == null) {
            return null;
        }
        return mode == ActualLoadMode.PER_SIDE ? load.multiply(BigDecimal.valueOf(2)) : load;
    }
}
