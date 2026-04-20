package com.tcc.dashboard_query_service.infrastructure.persistence.documents;

import com.tcc.dashboard_query_service.model.metrics.entities.ConsentMetrics;
import com.tcc.dashboard_query_service.model.metrics.valueObjects.CategoryMetrics;
import com.tcc.dashboard_query_service.model.metrics.valueObjects.FinalityMetrics;
import com.tcc.dashboard_query_service.model.metrics.valueObjects.TimeSeriesEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "consent_metrics")
public class ConsentMetricsDocument {
    @Id
    private String id;
    private long totalGranted;
    private long totalRevoked;
    private long activeConsents;
    private LocalDateTime lastUpdated;
    private String lastProcessedEventId;
    private List<FinalityMetrics> byFinality;
    private List<CategoryMetrics> byCategory;
    private List<TimeSeriesEntry> timeSeries;

    public static ConsentMetricsDocument fromDomain(ConsentMetrics metrics) {
        return ConsentMetricsDocument.builder()
                .id(metrics.getId())
                .totalGranted(metrics.getTotalGranted())
                .totalRevoked(metrics.getTotalRevoked())
                .activeConsents(metrics.getActiveConsents())
                .lastUpdated(metrics.getLastUpdated())
                .lastProcessedEventId(metrics.getLastProcessedEventId())
                .byFinality(metrics.getByFinality())
                .byCategory(metrics.getByCategory())
                .timeSeries(metrics.getTimeSeries())
                .build();
    }

    public ConsentMetrics toDomain() {
        return ConsentMetrics.builder()
                .id(this.id)
                .totalGranted(this.totalGranted)
                .totalRevoked(this.totalRevoked)
                .activeConsents(this.activeConsents)
                .lastUpdated(this.lastUpdated)
                .lastProcessedEventId(this.lastProcessedEventId)
                .byFinality(this.byFinality)
                .byCategory(this.byCategory)
                .timeSeries(this.timeSeries)
                .build();
    }
}