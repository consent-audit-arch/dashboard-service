package com.tcc.dashboard_query_service.application.services;

import com.tcc.dashboard_query_service.model.metrics.entities.ConsentMetrics;
import com.tcc.dashboard_query_service.model.metrics.enums.DataCategory;
import com.tcc.dashboard_query_service.model.metrics.enums.Purpose;
import com.tcc.dashboard_query_service.model.metrics.repositories.ConsentMetricsRepository;
import com.tcc.dashboard_query_service.model.metrics.valueObjects.CategoryMetrics;
import com.tcc.dashboard_query_service.model.metrics.valueObjects.FinalityMetrics;
import com.tcc.dashboard_query_service.model.metrics.valueObjects.TimeSeriesEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardQueryService {

    private final ConsentMetricsRepository metricsRepository;

    public ConsentMetrics getGlobalMetrics() {
        return metricsRepository.findGlobalMetrics()
                .orElse(ConsentMetrics.createInitial());
    }

    public List<TimeSeriesEntry> getTimeSeries(LocalDate from, LocalDate to) {
        return metricsRepository.findGlobalMetrics()
                .map(metrics -> metrics.getTimeSeries().stream()
                        .filter(entry -> !entry.getDate().isBefore(from) && !entry.getDate().isAfter(to))
                        .toList())
                .orElse(List.of());
    }

    public Optional<FinalityMetrics> getMetricsByPurpose(Purpose purpose) {
        return metricsRepository.findGlobalMetrics()
                .flatMap(metrics -> metrics.getByFinality().stream()
                        .filter(f -> f.getName().equals(purpose.name()))
                        .findFirst());
    }

    public Optional<CategoryMetrics> getMetricsByCategory(DataCategory category) {
        return metricsRepository.findGlobalMetrics()
                .flatMap(metrics -> metrics.getByCategory().stream()
                        .filter(c -> c.getName().equals(category.name()))
                        .findFirst());
    }
}