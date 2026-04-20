package com.tcc.dashboard_query_service.unit.application.service;

import com.tcc.dashboard_query_service.application.services.DashboardQueryService;
import com.tcc.dashboard_query_service.model.metrics.entities.ConsentMetrics;
import com.tcc.dashboard_query_service.model.metrics.enums.DataCategory;
import com.tcc.dashboard_query_service.model.metrics.enums.Purpose;
import com.tcc.dashboard_query_service.model.metrics.repositories.ConsentMetricsRepository;
import com.tcc.dashboard_query_service.model.metrics.valueObjects.CategoryMetrics;
import com.tcc.dashboard_query_service.model.metrics.valueObjects.FinalityMetrics;
import com.tcc.dashboard_query_service.model.metrics.valueObjects.TimeSeriesEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardQueryServiceTest {

    @Mock
    private ConsentMetricsRepository metricsRepository;

    private DashboardQueryService queryService;

    private ConsentMetrics metricsWithData;

    @BeforeEach
    void setUp() {
        queryService = new DashboardQueryService(metricsRepository);

        FinalityMetrics promotionMetrics = FinalityMetrics.builder()
                .name("PROMOTION")
                .granted(10)
                .revoked(2)
                .active(8)
                .revokeRate(20.0)
                .build();

        FinalityMetrics analyticsMetrics = FinalityMetrics.builder()
                .name("ANALYTICS")
                .granted(5)
                .revoked(1)
                .active(4)
                .revokeRate(20.0)
                .build();

        CategoryMetrics personalDataMetrics = CategoryMetrics.builder()
                .name("PERSONAL_DATA")
                .granted(8)
                .revoked(2)
                .active(6)
                .revokeRate(25.0)
                .build();

        CategoryMetrics contractDataMetrics = CategoryMetrics.builder()
                .name("CONTRACT_DATA")
                .granted(7)
                .revoked(1)
                .active(6)
                .revokeRate(14.28)
                .build();

        TimeSeriesEntry entry1 = TimeSeriesEntry.builder()
                .date(LocalDate.of(2026, 4, 1))
                .granted(5)
                .revoked(1)
                .activeAtEnd(4)
                .build();

        TimeSeriesEntry entry2 = TimeSeriesEntry.builder()
                .date(LocalDate.of(2026, 4, 15))
                .granted(10)
                .revoked(2)
                .activeAtEnd(12)
                .build();

        metricsWithData = ConsentMetrics.builder()
                .id(ConsentMetrics.GLOBAL_ID)
                .totalGranted(15)
                .totalRevoked(3)
                .activeConsents(12)
                .lastUpdated(LocalDateTime.now())
                .byFinality(List.of(promotionMetrics, analyticsMetrics))
                .byCategory(List.of(personalDataMetrics, contractDataMetrics))
                .timeSeries(List.of(entry1, entry2))
                .build();
    }

    @Test
    void getGlobalMetrics_shouldReturnMetricsWhenFound() {
        when(metricsRepository.findGlobalMetrics()).thenReturn(Optional.of(metricsWithData));

        ConsentMetrics result = queryService.getGlobalMetrics();

        assertThat(result.getTotalGranted()).isEqualTo(15);
        assertThat(result.getTotalRevoked()).isEqualTo(3);
        assertThat(result.getActiveConsents()).isEqualTo(12);
    }

    @Test
    void getGlobalMetrics_shouldReturnInitialWhenNotFound() {
        when(metricsRepository.findGlobalMetrics()).thenReturn(Optional.empty());

        ConsentMetrics result = queryService.getGlobalMetrics();

        assertThat(result.getTotalGranted()).isEqualTo(0);
        assertThat(result.getTotalRevoked()).isEqualTo(0);
    }

    @Test
    void getTimeSeries_shouldFilterByDateRange() {
        when(metricsRepository.findGlobalMetrics()).thenReturn(Optional.of(metricsWithData));

        List<TimeSeriesEntry> result = queryService.getTimeSeries(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 10));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 4, 1));
    }

    @Test
    void getTimeSeries_shouldReturnEmptyListWhenNoMetrics() {
        when(metricsRepository.findGlobalMetrics()).thenReturn(Optional.empty());

        List<TimeSeriesEntry> result = queryService.getTimeSeries(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30));

        assertThat(result).isEmpty();
    }

    @Test
    void getTimeSeries_shouldReturnAllWithinRange() {
        when(metricsRepository.findGlobalMetrics()).thenReturn(Optional.of(metricsWithData));

        List<TimeSeriesEntry> result = queryService.getTimeSeries(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30));

        assertThat(result).hasSize(2);
    }

    @Test
    void getMetricsByPurpose_shouldReturnMetricsWhenFound() {
        when(metricsRepository.findGlobalMetrics()).thenReturn(Optional.of(metricsWithData));

        var result = queryService.getMetricsByPurpose(Purpose.PROMOTION);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("PROMOTION");
        assertThat(result.get().getGranted()).isEqualTo(10);
    }

    @Test
    void getMetricsByPurpose_shouldReturnEmptyWhenNotFound() {
        when(metricsRepository.findGlobalMetrics()).thenReturn(Optional.of(metricsWithData));

        var result = queryService.getMetricsByPurpose(Purpose.BILLING);

        assertThat(result).isEmpty();
    }

    @Test
    void getMetricsByCategory_shouldReturnMetricsWhenFound() {
        when(metricsRepository.findGlobalMetrics()).thenReturn(Optional.of(metricsWithData));

        var result = queryService.getMetricsByCategory(DataCategory.PERSONAL_DATA);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("PERSONAL_DATA");
        assertThat(result.get().getGranted()).isEqualTo(8);
    }

    @Test
    void getMetricsByCategory_shouldReturnEmptyWhenNotFound() {
        when(metricsRepository.findGlobalMetrics()).thenReturn(Optional.of(metricsWithData));

        var result = queryService.getMetricsByCategory(DataCategory.HEALTH_DATA);

        assertThat(result).isEmpty();
    }
}