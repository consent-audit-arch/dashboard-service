package com.tcc.dashboard_query_service.infrastructure.persistence;

import com.tcc.dashboard_query_service.infrastructure.persistence.documents.ConsentMetricsDocument;
import com.tcc.dashboard_query_service.infrastructure.persistence.repositories.ConsentMetricsMongoRepository;
import com.tcc.dashboard_query_service.model.metrics.entities.ConsentMetrics;
import com.tcc.dashboard_query_service.model.metrics.enums.DataCategory;
import com.tcc.dashboard_query_service.model.metrics.enums.Purpose;
import com.tcc.dashboard_query_service.model.metrics.repositories.ConsentMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ConsentMetricsRepositoryImpl implements ConsentMetricsRepository {

    private static final String GLOBAL_ID = "global";

    private final ConsentMetricsMongoRepository mongoRepository;

    @Override
    public Optional<ConsentMetrics> findGlobalMetrics() {
        return mongoRepository.findById(GLOBAL_ID)
                .map(ConsentMetricsDocument::toDomain);
    }

    @Override
    public ConsentMetrics save(ConsentMetrics metrics) {
        ConsentMetricsDocument document = ConsentMetricsDocument.fromDomain(metrics);
        ConsentMetricsDocument saved = mongoRepository.save(document);
        return saved.toDomain();
    }

    @Override
    public void updateLastProcessedEventId(String eventId) {
        Optional<ConsentMetricsDocument> existing = mongoRepository.findById(GLOBAL_ID);
        if (existing.isPresent()) {
            ConsentMetricsDocument doc = existing.get();
            doc.setLastProcessedEventId(eventId);
            mongoRepository.save(doc);
        }
    }

    @Override
    public Optional<ConsentMetrics> findByPurpose(Purpose purpose) {
        return findGlobalMetrics();
    }

    @Override
    public Optional<ConsentMetrics> findByCategory(DataCategory category) {
        return findGlobalMetrics();
    }

    @Override
    public List<ConsentMetrics> findTimeSeries(LocalDate from, LocalDate to) {
        return findGlobalMetrics()
                .map(metrics -> metrics.getTimeSeries().stream()
                        .filter(entry -> !entry.getDate().isBefore(from) && !entry.getDate().isAfter(to))
                        .collect(Collectors.toList()))
                .map(list -> {
                    ConsentMetrics filtered = ConsentMetrics.createInitial();
                    filtered.getTimeSeries().addAll(list);
                    return filtered;
                })
                .map(List::of)
                .orElse(List.of());
    }
}