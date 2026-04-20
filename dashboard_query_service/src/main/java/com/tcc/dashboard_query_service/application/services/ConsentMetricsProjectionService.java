package com.tcc.dashboard_query_service.application.services;

import com.tcc.dashboard_query_service.infrastructure.messaging.DTOs.ConsentEventMessage;
import com.tcc.dashboard_query_service.model.metrics.entities.ConsentMetrics;
import com.tcc.dashboard_query_service.model.metrics.repositories.ConsentMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentMetricsProjectionService {

    private final ConsentMetricsRepository metricsRepository;
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    public void processEvent(ConsentEventMessage message) {
        String eventId = message.getEventId();
        
        if (eventId == null || eventId.isEmpty()) {
            String dataCategory = message.getDataCategory() != null ? message.getDataCategory().name() : "null";
            String purpose = message.getPurpose() != null ? message.getPurpose().name() : "null";
            String eventType = message.getEventType() != null ? message.getEventType() : "null";
            eventId = message.getConsentId() + ":" + dataCategory + ":" + purpose + ":" + eventType;
        }
        
        if (!processedEventIds.add(eventId)) {
            log.info("Evento {} já processado, ignorando", eventId);
            return;
        }
        
        log.info("Processando evento: eventId={}, eventType={}, category={}, purpose={}", 
                eventId, message.getEventType(), message.getDataCategory(), message.getPurpose());
        
        ConsentMetrics metrics = metricsRepository.findGlobalMetrics()
                .orElseGet(ConsentMetrics::createInitial);
        
        LocalDate eventDate = message.getOccurredAt() != null 
                ? message.getOccurredAt().toLocalDate() 
                : LocalDate.now();
        
        if (message.isConsentGranted()) {
            metrics.incrementGranted(
                    message.getDataCategory(),
                    message.getPurpose(),
                    eventDate
            );
            log.info("ConsentGranted processado: category={}, purpose={}, date={}", 
                    message.getDataCategory(), message.getPurpose(), eventDate);
        } else if (message.isConsentRevoked()) {
            metrics.incrementRevoked(
                    message.getDataCategory(),
                    message.getPurpose(),
                    eventDate
            );
            log.info("ConsentRevoked processado: category={}, purpose={}, date={}", 
                    message.getDataCategory(), message.getPurpose(), eventDate);
        } else {
            log.warn("Tipo de evento desconhecido: {}", message.getEventType());
            return;
        }
        
        metricsRepository.save(metrics);
        log.info("Métricas atualizadas com sucesso");
    }
}