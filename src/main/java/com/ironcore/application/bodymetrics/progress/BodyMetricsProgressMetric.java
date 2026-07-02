package com.ironcore.application.bodymetrics.progress;

import java.util.function.Function;

import static com.ironcore.application.bodymetrics.progress.BodyMetricsProgressChartType.*;

public enum BodyMetricsProgressMetric {

    WEIGHT_KG("Peso", "kg", BODY_COMPOSITION, true, BodyMetricsProgressProjection::weightKg),
    FAT_MASS_KG("Massa gorda", "kg", BODY_COMPOSITION, true, BodyMetricsProgressProjection::fatMassKg),
    LEAN_MASS_KG("Massa magra", "kg", BODY_COMPOSITION, true, BodyMetricsProgressProjection::leanMassKg),

    BODY_FAT_PERCENTAGE("Gordura corporal", "%", BODY_FAT, true, BodyMetricsProgressProjection::bodyFatPercentage),

    BMI("IMC", "", null, true, BodyMetricsProgressProjection::bmi),

    NECK_CM("Pescoço", "cm", CIRCUMFERENCES, true, BodyMetricsProgressProjection::neckCm),
    CHEST_CM("Peitoral", "cm", CIRCUMFERENCES, true, BodyMetricsProgressProjection::chestCm),
    SHOULDER_CM("Ombro", "cm", CIRCUMFERENCES, true, BodyMetricsProgressProjection::shoulderCm),
    ARM_CM("Braço", "cm", CIRCUMFERENCES, true, BodyMetricsProgressProjection::armCm),
    FOREARM_CM("Antebraço", "cm", CIRCUMFERENCES, true, BodyMetricsProgressProjection::forearmCm),
    WAIST_CM("Cintura", "cm", CIRCUMFERENCES, true, BodyMetricsProgressProjection::waistCm),
    HIP_CM("Quadril", "cm", CIRCUMFERENCES, true, BodyMetricsProgressProjection::hipCm),
    THIGH_CM("Coxa", "cm", CIRCUMFERENCES, true, BodyMetricsProgressProjection::thighCm),
    CALF_CM("Panturrilha", "cm", CIRCUMFERENCES, true, BodyMetricsProgressProjection::calfCm);

    private final String label;
    private final String unit;
    private final BodyMetricsProgressChartType chartType;
    private final boolean changeTableEnabled;
    private final Function<BodyMetricsProgressProjection, Double> valueExtractor;

    BodyMetricsProgressMetric(
            String label,
            String unit,
            BodyMetricsProgressChartType chartType,
            boolean changeTableEnabled,
            Function<BodyMetricsProgressProjection, Double> valueExtractor
    ) {
        this.label = label;
        this.unit = unit;
        this.chartType = chartType;
        this.changeTableEnabled = changeTableEnabled;
        this.valueExtractor = valueExtractor;
    }

    public Double extractValue(BodyMetricsProgressProjection projection) {
        return valueExtractor.apply(projection);
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
