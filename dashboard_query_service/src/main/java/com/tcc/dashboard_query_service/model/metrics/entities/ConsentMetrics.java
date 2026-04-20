package com.tcc.dashboard_query_service.model.metrics.entities;

import com.tcc.dashboard_query_service.model.metrics.enums.DataCategory;
import com.tcc.dashboard_query_service.model.metrics.enums.Purpose;
import com.tcc.dashboard_query_service.model.metrics.valueObjects.FinalityMetrics;
import com.tcc.dashboard_query_service.model.metrics.valueObjects.CategoryMetrics;
import com.tcc.dashboard_query_service.model.metrics.valueObjects.TimeSeriesEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentMetrics {
    public static final String GLOBAL_ID = "global";

    private String id;
    private long totalGranted;
    private long totalRevoked;
    private long activeConsents;
    private LocalDateTime lastUpdated;
    private String lastProcessedEventId;
    
    @Builder.Default
    private List<FinalityMetrics> byFinality = new ArrayList<>();
    
    @Builder.Default
    private List<CategoryMetrics> byCategory = new ArrayList<>();
    
    @Builder.Default
    private List<TimeSeriesEntry> timeSeries = new ArrayList<>();

    public static ConsentMetrics createInitial() {
        return ConsentMetrics.builder()
                .id(GLOBAL_ID)
                .totalGranted(0)
                .totalRevoked(0)
                .activeConsents(0)
                .lastUpdated(null)
                .lastProcessedEventId(null)
                .byFinality(new ArrayList<>())
                .byCategory(new ArrayList<>())
                .timeSeries(new ArrayList<>())
                .build();
    }

    public void incrementGranted(DataCategory category, Purpose purpose, LocalDate eventDate) {
        this.totalGranted++;
        this.activeConsents = this.totalGranted - this.totalRevoked;
        this.lastUpdated = LocalDateTime.now();
        
        updateFinality(purpose, true);
        updateCategory(category, true);
        updateTimeSeries(eventDate, true);
    }

    public void incrementRevoked(DataCategory category, Purpose purpose, LocalDate eventDate) {
        this.totalRevoked++;
        this.activeConsents = this.totalGranted - this.totalRevoked;
        this.lastUpdated = LocalDateTime.now();
        
        updateFinality(purpose, false);
        updateCategory(category, false);
        updateTimeSeries(eventDate, false);
    }

    private void updateFinality(Purpose purpose, boolean granted) {
        FinalityMetrics finalityMetric = byFinality.stream()
                .filter(f -> f.getName().equals(purpose.name()))
                .findFirst()
                .orElse(null);
        
        if (finalityMetric == null) {
            finalityMetric = FinalityMetrics.builder()
                    .name(purpose.name())
                    .granted(0)
                    .revoked(0)
                    .active(0)
                    .revokeRate(0.0)
                    .build();
            byFinality.add(finalityMetric);
        }
        
        finalityMetric.setGranted(finalityMetric.getGranted() + (granted ? 1 : 0));
        finalityMetric.setRevoked(finalityMetric.getRevoked() + (granted ? 0 : 1));
        finalityMetric.setActive(finalityMetric.getGranted() - finalityMetric.getRevoked());
        finalityMetric.setRevokeRate(calculateRevokeRate(finalityMetric.getGranted(), finalityMetric.getRevoked()));
    }

    private void updateCategory(DataCategory category, boolean granted) {
        CategoryMetrics categoryMetric = byCategory.stream()
                .filter(c -> c.getName().equals(category.name()))
                .findFirst()
                .orElse(null);
        
        if (categoryMetric == null) {
            categoryMetric = CategoryMetrics.builder()
                    .name(category.name())
                    .granted(0)
                    .revoked(0)
                    .active(0)
                    .revokeRate(0.0)
                    .build();
            byCategory.add(categoryMetric);
        }
        
        categoryMetric.setGranted(categoryMetric.getGranted() + (granted ? 1 : 0));
        categoryMetric.setRevoked(categoryMetric.getRevoked() + (granted ? 0 : 1));
        categoryMetric.setActive(categoryMetric.getGranted() - categoryMetric.getRevoked());
        categoryMetric.setRevokeRate(calculateRevokeRate(categoryMetric.getGranted(), categoryMetric.getRevoked()));
    }

    private void updateTimeSeries(LocalDate date, boolean granted) {
        TimeSeriesEntry entry = timeSeries.stream()
                .filter(t -> t.getDate().equals(date))
                .findFirst()
                .orElse(null);
        
        if (entry == null) {
            entry = TimeSeriesEntry.builder()
                    .date(date)
                    .granted(0)
                    .revoked(0)
                    .activeAtEnd(0)
                    .build();
            timeSeries.add(entry);
        }
        
        if (granted) {
            entry.setGranted(entry.getGranted() + 1);
            entry.setActiveAtEnd(entry.getActiveAtEnd() + 1);
        } else {
            entry.setRevoked(entry.getRevoked() + 1);
            entry.setActiveAtEnd(entry.getActiveAtEnd() - 1);
        }
    }

    private double calculateRevokeRate(long granted, long revoked) {
        if (granted == 0) return 0.0;
        return (double) revoked / granted * 100.0;
    }

    public TimeSeriesEntry getTimeSeriesEntry(LocalDate date) {
        return timeSeries.stream()
                .filter(t -> t.getDate().equals(date))
                .findFirst()
                .orElse(null);
    }
}