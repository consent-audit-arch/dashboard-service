package com.tcc.dashboard_query_service.model.metrics.valueObjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalityMetrics {
    private String name;
    private long granted;
    private long revoked;
    private long active;
    private double revokeRate;
}