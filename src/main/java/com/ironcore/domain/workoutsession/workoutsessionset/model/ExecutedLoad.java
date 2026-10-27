package com.ironcore.domain.workoutsession.workoutsessionset.model;

import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import java.math.BigDecimal;

public final class ExecutedLoad {
    private ExecutedLoad() {}
    public static BigDecimal normalize(BigDecimal load, ActualLoadMode mode) {
        if (load == null) return null;
        return mode == ActualLoadMode.PER_SIDE ? load.multiply(BigDecimal.valueOf(2)) : load;
    }
}
