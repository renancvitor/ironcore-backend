package com.ironcore.infrastructure.config;

import com.ironcore.domain.userbodymetrics.service.BMICalculator;
import com.ironcore.domain.userbodymetrics.service.FatMassCalculator;
import com.ironcore.domain.userbodymetrics.service.LeanMassCalculator;
import com.ironcore.domain.userbodymetrics.service.NavyBodyFatCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BodyMetricsCalculatorConfig {

    @Bean
    public BMICalculator bmiCalculator() {
        return new BMICalculator();
    }

    @Bean
    public NavyBodyFatCalculator navyBodyFatCalculator() {
        return new NavyBodyFatCalculator();
    }

    @Bean
    public FatMassCalculator fatMassCalculator() {
        return new FatMassCalculator();
    }

    @Bean
    public LeanMassCalculator leanMassCalculator() {
        return new LeanMassCalculator();
    }
}
