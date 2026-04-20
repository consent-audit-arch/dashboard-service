package com.tcc.dashboard_query_service.infrastructure.messaging.consumers;

import com.tcc.dashboard_query_service.application.services.ConsentMetricsProjectionService;
import com.tcc.dashboard_query_service.infrastructure.messaging.DTOs.ConsentEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsentEventConsumer {

    private final ConsentMetricsProjectionService projectionService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ConsentEventConsumer(ConsentMetricsProjectionService projectionService, ObjectMapper objectMapper) {
        this.projectionService = projectionService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "ev-consent",
            groupId = "dashboard-service-group",
            containerFactory = "consentEventKafkaListenerContainerFactory"
    )
    public void consume(String payload) {
        try {
            ConsentEventMessage message = objectMapper.readValue(payload, ConsentEventMessage.class);
            log.info("Recebido evento: eventId={}, eventType={}", message.getEventId(), message.getEventType());
            
            projectionService.processEvent(message);
            log.info("Evento processado com sucesso: eventId={}", message.getEventId());
        } catch (Exception e) {
            log.error("Erro ao processar evento: {}", e.getMessage(), e);
        }
    }
}