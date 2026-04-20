package com.tcc.dashboard_query_service.model.metrics.valueObjects;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MetricsSnapshot {
    private final long totalGranted;
    private final long totalRevoked;
    private final long activeConsents;
    private final java.time.LocalDateTime lastUpdated;

    public static MetricsSnapshot empty() {
        return MetricsSnapshot.builder()
                .totalGranted(0)
                .totalRevoked(0)
                .activeConsents(0)
                .lastUpdated(null)
                .build();
    }

    public MetricsSnapshot withGranted() {
        return MetricsSnapshot.builder()
                .totalGranted(this.totalGranted + 1)
                .totalRevoked(this.totalRevoked)
                .activeConsents(this.activeConsents + 1)
                .lastUpdated(java.time.LocalDateTime.now())
                .build();
    }

    public MetricsSnapshot withRevoked() {
        return MetricsSnapshot.builder()
                .totalGranted(this.totalGranted)
                .totalRevoked(this.totalRevoked + 1)
                .activeConsents(this.totalGranted - this.totalRevoked - 1)
                .lastUpdated(java.time.LocalDateTime.now())
                .build();
    }
}