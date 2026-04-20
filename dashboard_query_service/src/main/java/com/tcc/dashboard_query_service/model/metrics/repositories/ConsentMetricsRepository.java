package com.tcc.dashboard_query_service.model.metrics.repositories;

import com.tcc.dashboard_query_service.model.metrics.entities.ConsentMetrics;
import com.tcc.dashboard_query_service.model.metrics.enums.DataCategory;
import com.tcc.dashboard_query_service.model.metrics.enums.Purpose;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConsentMetricsRepository {
    Optional<ConsentMetrics> findGlobalMetrics();
    ConsentMetrics save(ConsentMetrics metrics);
    void updateLastProcessedEventId(String eventId);
    Optional<ConsentMetrics> findByPurpose(Purpose purpose);
    Optional<ConsentMetrics> findByCategory(DataCategory category);
    List<ConsentMetrics> findTimeSeries(LocalDate from, LocalDate to);
}