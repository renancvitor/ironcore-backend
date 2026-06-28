package com.ironcore.application.userbodymetrics.progress;

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
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GetBodyMetricsProgressChangesUseCase {

    private final UserRepository userRepository;
    private final BodyMetricsProgressQueryPort queryPort;

    @Transactional(readOnly = true)
    public GetBodyMetricsProgressChangesResult execute(BodyMetricsProgressChangesCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        BodyMetricsProgressPeriodValidator.validate(command.startDate(), command.endDate());

        LocalDate startDate = command.startDate();
        LocalDate endDate = command.endDate();

        List<BodyMetricsProgressProjection> progress = queryPort.findProgressData(
                user.getId(),
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        );

        List<BodyMetricsProgressChangeResult> changes = Arrays.stream(BodyMetricsProgressMetric.values())
                .filter(BodyMetricsProgressMetric::changeTableEnabled)
                .map(metric -> {
                    List<MetricValuePoint> points = progress.stream()
                            .map(projection -> new MetricValuePoint(
                                    projection.measuredAt().toLocalDate(),
                                    metric.extractValue(projection)
                            ))
                            .filter(point -> point.value() != null && point.value() > 0.0)
                            .toList();

                    if (points.size() < 2) {
                        return null;
                    }

                    MetricValuePoint first = points.getFirst();
                    MetricValuePoint last = points.getLast();

                    Double absoluteChange = last.value() - first.value();
                    Double percentageChange = (absoluteChange / first.value()) * 100;

                    return new BodyMetricsProgressChangeResult(
                            metric,
                            metric.label(),
                            metric.unit(),
                            first.date(),
                            first.value(),
                            last.date(),
                            last.value(),
                            absoluteChange,
                            percentageChange
                    );
                })
                .filter(Objects::nonNull)
                .toList();

        return new GetBodyMetricsProgressChangesResult(
                startDate,
                endDate,
                changes
        );
    }

    private record MetricValuePoint(
            LocalDate date,
            Double value
    ) {}
}
