package com.tcc.dashboard_query_service.model.metrics.valueObjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesEntry {
    private LocalDate date;
    private long granted;
    private long revoked;
    private long activeAtEnd;
}