package com.tcc.dashboard_query_service.application.controllers;

import com.tcc.dashboard_query_service.application.services.DashboardQueryService;
import com.tcc.dashboard_query_service.model.metrics.entities.ConsentMetrics;
import com.tcc.dashboard_query_service.model.metrics.enums.DataCategory;
import com.tcc.dashboard_query_service.model.metrics.enums.Purpose;
import com.tcc.dashboard_query_service.model.metrics.valueObjects.CategoryMetrics;
import com.tcc.dashboard_query_service.model.metrics.valueObjects.FinalityMetrics;
import com.tcc.dashboard_query_service.model.metrics.valueObjects.TimeSeriesEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard/metrics")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardQueryService queryService;

    @GetMapping
    public ResponseEntity<ConsentMetrics> getGlobalMetrics() {
        return ResponseEntity.ok(queryService.getGlobalMetrics());
    }

    @GetMapping("/timeseries")
    public ResponseEntity<Map<String, Object>> getTimeSeries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        List<TimeSeriesEntry> data = queryService.getTimeSeries(from, to);
        
        return ResponseEntity.ok(Map.of(
                "period", Map.of("from", from.toString(), "to", to.toString()),
                "data", data
        ));
    }

    @GetMapping("/finality/{finality}")
    public ResponseEntity<FinalityMetrics> getMetricsByFinality(@PathVariable String finality) {
        Purpose purpose = Purpose.valueOf(finality.toUpperCase());
        return queryService.getMetricsByPurpose(purpose)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{dataCategory}")
    public ResponseEntity<CategoryMetrics> getMetricsByCategory(@PathVariable String dataCategory) {
        DataCategory category = DataCategory.valueOf(dataCategory.toUpperCase());
        return queryService.getMetricsByCategory(category)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}