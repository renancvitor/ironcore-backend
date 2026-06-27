package com.ironcore.application.userbodymetrics.progress;

import com.ironcore.application.exception.BusinessRuleViolationException;
import com.ironcore.application.exception.OperationNotAllowedException;
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
import java.util.Arrays;
import java.util.List;

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

        if (command.startDate() == null || command.endDate() == null) {
            throw new BusinessRuleViolationException("As datas são obrigatórias.");
        }

        if (command.startDate().isAfter(command.endDate())) {
            throw new OperationNotAllowedException("Data inicial não pode ser maior do que data final.");
        }

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
                    List<BodyMetricsProgressPointResult> points = progress.stream()
                            .map(projection -> new ChartPoint(
                                    projection.measuredAt().toLocalDate().toString(),
                                    metric.extractValue(projection)
                            ))
                            .filter(point -> point.value() != null && point.value() > 0.0)
                            .map(point -> new BodyMetricsProgressPointResult(
                                    point.period(),
                                    point.value()
                            ))
                            .toList();

                    return new BodyMetricsProgressSeriesResult(
                            metric,
                            metric.label(),
                            metric.unit(),
                            points
                    );
                })
                .toList();

        return new GetBodyMetricsProgressChartResult(
                startDate,
                endDate,
                chartType,
                series
        );
    }

    private record ChartPoint(
            String period,
            Double value
    ) {}
}
