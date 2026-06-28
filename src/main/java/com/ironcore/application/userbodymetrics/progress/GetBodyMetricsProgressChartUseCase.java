package com.ironcore.application.userbodymetrics.progress;

import com.ironcore.application.exception.BusinessRuleViolationException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.userbodymetrics.port.BodyMetricsProgressQueryPort;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetBodyMetricsProgressChartUseCase {

    private final UserRepository userRepository;
    private final BodyMetricsProgressQueryPort queryPort;

    @Transactional(readOnly = true)
    public GetBodyMetricsProgressChartResult execute(BodyMetricsProgressChartCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        if (command.chartType() == null) {
            throw new BusinessRuleViolationException("Tipo do gráfico é obrigatório.");
        }

        BodyMetricsProgressPeriodValidator.validate(command.startDate(), command.endDate());

        LocalDate startDate = command.startDate();
        LocalDate endDate = command.endDate();

        List<BodyMetricsProgressProjection> progress = queryPort.findProgressData(
                user.getId(),
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        );

        BodyMetricsProgressChartType chartType = command.chartType();

        List<BodyMetricsProgressSeriesResult> series = Arrays.stream(BodyMetricsProgressMetric.values())
                .filter(metric -> metric.belongsToChart(chartType))
                .map(metric -> {
                    Map<YearMonth, List<ChartPoint>> pointsByMonth = progress.stream()
                            .map(projection -> new ChartPoint(
                                    YearMonth.from(projection.measuredAt()),
                                    metric.extractValue(projection)
                            ))
                            .filter(point -> point.value() != null && point.value() > 0.0)
                            .collect(Collectors.groupingBy(
                                    ChartPoint::period,
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));
                    
                    List<BodyMetricsProgressPointResult> points = pointsByMonth.entrySet().stream()
                            .map(entry -> {
                                ChartPoint lastPointOfMonth = entry.getValue().getLast();

                                return new BodyMetricsProgressPointResult(
                                        entry.getKey().toString(),
                                        lastPointOfMonth.value()
                                );
                            })
                            .toList();

                    return new BodyMetricsProgressSeriesResult(
                            metric,
                            metric.label(),
                            metric.unit(),
                            points
                    );
                })
                .filter(seriesResult -> !seriesResult.points().isEmpty())
                .toList();

        return new GetBodyMetricsProgressChartResult(
                startDate,
                endDate,
                chartType,
                series
        );
    }

    private record ChartPoint(
            YearMonth period,
            Double value
    ) {}
}
