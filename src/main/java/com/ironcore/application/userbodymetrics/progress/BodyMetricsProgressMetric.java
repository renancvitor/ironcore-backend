package com.ironcore.application.userbodymetrics.progress;

import static com.ironcore.application.userbodymetrics.progress.BodyMetricsProgressChartType.*;

public enum BodyMetricsProgressMetric {

    WEIGHT_KG("Peso", "kg", BODY_COMPOSITION, true),
    FAT_MASS_KG("Massa gorda", "kg", BODY_COMPOSITION, true),
    LEAN_MASS_KG("Massa magra", "kg", BODY_COMPOSITION, true),

    BODY_FAT_PERCENTAGE("Gordura corporal", "%", BODY_FAT, true),

    BMI("IMC", "", null, false),

    NECK_CM("Pescoço", "cm", CIRCUMFERENCES, true),
    CHEST_CM("Peitoral", "cm", CIRCUMFERENCES, true),
    SHOULDER_CM("Ombro", "cm", CIRCUMFERENCES, true),
    ARM_CM("Braço", "cm", CIRCUMFERENCES, true),
    FOREARM_CM("Antebraço", "cm", CIRCUMFERENCES, true),
    WAIST_CM("Cintura", "cm", CIRCUMFERENCES, true),
    HIP_CM("Quadril", "cm", CIRCUMFERENCES, true),
    THIGH_CM("Coxa", "cm", CIRCUMFERENCES, true),
    CALF_CM("Panturrilha", "cm", CIRCUMFERENCES, true);

    private final String label;
    private final String unit;
    private final BodyMetricsProgressChartType chartType;
    private final boolean changeTableEnabled;

    BodyMetricsProgressMetric(
            String label,
            String unit,
            BodyMetricsProgressChartType chartType,
            boolean changeTableEnabled
    ) {
        this.label = label;
        this.unit = unit;
        this.chartType = chartType;
        this.changeTableEnabled = changeTableEnabled;
    }

    public boolean belongsToChart(BodyMetricsProgressChartType chartType) {
        return this.chartType != null && this.chartType == chartType;
    }

    public boolean changeTableEnabled() {
        return changeTableEnabled;
    }

    public String label() {
        return label;
    }

    public String unit() {
        return unit;
    }
}
