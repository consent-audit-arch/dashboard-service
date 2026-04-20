package com.tcc.dashboard_query_service.unit.application.service;

import com.tcc.dashboard_query_service.application.services.ConsentMetricsProjectionService;
import com.tcc.dashboard_query_service.infrastructure.messaging.DTOs.ConsentEventMessage;
import com.tcc.dashboard_query_service.model.metrics.entities.ConsentMetrics;
import com.tcc.dashboard_query_service.model.metrics.enums.DataCategory;
import com.tcc.dashboard_query_service.model.metrics.enums.Purpose;
import com.tcc.dashboard_query_service.model.metrics.repositories.ConsentMetricsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentMetricsProjectionServiceTest {

    @Mock
    private ConsentMetricsRepository metricsRepository;

    private ConsentMetricsProjectionService projectionService;

    private ConsentMetrics savedMetrics;

    @BeforeEach
    void setUp() {
        projectionService = new ConsentMetricsProjectionService(metricsRepository);
        savedMetrics = ConsentMetrics.createInitial();
        when(metricsRepository.findGlobalMetrics()).thenReturn(Optional.of(savedMetrics));
    }

    @Test
    void processEvent_shouldIncrementGrantedForConsentGranted() {
        ConsentEventMessage message = ConsentEventMessage.builder()
                .consentId("test-consent-1")
                .eventType("ConsentGranted")
                .ownerId(1L)
                .dataCategory(DataCategory.PERSONAL_DATA)
                .purpose(Purpose.PROMOTION)
                .legalBasis("CONSENT")
                .occurredAt(LocalDateTime.now())
                .build();

        projectionService.processEvent(message);
        ArgumentCaptor<ConsentMetrics> captor = ArgumentCaptor.forClass(ConsentMetrics.class);
        verify(metricsRepository).save(captor.capture());

        ConsentMetrics result = captor.getValue();
        assertThat(result.getTotalGranted()).isEqualTo(1);
        assertThat(result.getTotalRevoked()).isEqualTo(0);
        assertThat(result.getActiveConsents()).isEqualTo(1);
    }

    @Test
    void processEvent_shouldIncrementRevokedForConsentRevoked() {
        when(metricsRepository.findGlobalMetrics()).thenReturn(Optional.of(
                ConsentMetrics.builder()
                        .id(ConsentMetrics.GLOBAL_ID)
                        .totalGranted(1)
                        .totalRevoked(0)
                        .activeConsents(1)
                        .build()
        ));

        ConsentEventMessage message = ConsentEventMessage.builder()
                .consentId("test-consent-1")
                .eventType("ConsentRevoked")
                .ownerId(1L)
                .dataCategory(DataCategory.PERSONAL_DATA)
                .purpose(Purpose.PROMOTION)
                .legalBasis("CONSENT")
                .occurredAt(LocalDateTime.now())
                .build();

        projectionService.processEvent(message);
        ArgumentCaptor<ConsentMetrics> captor = ArgumentCaptor.forClass(ConsentMetrics.class);
        verify(metricsRepository).save(captor.capture());

        ConsentMetrics result = captor.getValue();
        assertThat(result.getTotalGranted()).isEqualTo(1);
        assertThat(result.getTotalRevoked()).isEqualTo(1);
        assertThat(result.getActiveConsents()).isEqualTo(0);
    }

    @Test
    void processEvent_shouldIgnoreDuplicateEvents() {
        ConsentEventMessage message = ConsentEventMessage.builder()
                .consentId("test-consent-1")
                .eventType("ConsentGranted")
                .ownerId(1L)
                .dataCategory(DataCategory.PERSONAL_DATA)
                .purpose(Purpose.PROMOTION)
                .legalBasis("CONSENT")
                .occurredAt(LocalDateTime.now())
                .build();

        projectionService.processEvent(message);
        projectionService.processEvent(message);

        verify(metricsRepository, times(1)).save(any(ConsentMetrics.class));
    }

    @Test
    void processEvent_shouldUpdateFinalityMetrics() {
        ConsentEventMessage message = ConsentEventMessage.builder()
                .consentId("test-consent-1")
                .eventType("ConsentGranted")
                .ownerId(1L)
                .dataCategory(DataCategory.PERSONAL_DATA)
                .purpose(Purpose.PROMOTION)
                .legalBasis("CONSENT")
                .occurredAt(LocalDateTime.now())
                .build();

        projectionService.processEvent(message);
        ArgumentCaptor<ConsentMetrics> captor = ArgumentCaptor.forClass(ConsentMetrics.class);
        verify(metricsRepository).save(captor.capture());

        ConsentMetrics result = captor.getValue();
        assertThat(result.getByFinality()).hasSize(1);
        assertThat(result.getByFinality().get(0).getName()).isEqualTo("PROMOTION");
        assertThat(result.getByFinality().get(0).getGranted()).isEqualTo(1);
    }

    @Test
    void processEvent_shouldUpdateCategoryMetrics() {
        ConsentEventMessage message = ConsentEventMessage.builder()
                .consentId("test-consent-1")
                .eventType("ConsentGranted")
                .ownerId(1L)
                .dataCategory(DataCategory.CONTRACT_DATA)
                .purpose(Purpose.ANALYTICS)
                .legalBasis("CONSENT")
                .occurredAt(LocalDateTime.now())
                .build();

        projectionService.processEvent(message);
        ArgumentCaptor<ConsentMetrics> captor = ArgumentCaptor.forClass(ConsentMetrics.class);
        verify(metricsRepository).save(captor.capture());

        ConsentMetrics result = captor.getValue();
        assertThat(result.getByCategory()).hasSize(1);
        assertThat(result.getByCategory().get(0).getName()).isEqualTo("CONTRACT_DATA");
        assertThat(result.getByCategory().get(0).getGranted()).isEqualTo(1);
    }

    @Test
    void processEvent_shouldCreateInitialMetricsWhenNotFound() {
        when(metricsRepository.findGlobalMetrics()).thenReturn(Optional.empty());

        ConsentEventMessage message = ConsentEventMessage.builder()
                .consentId("test-consent-1")
                .eventType("ConsentGranted")
                .ownerId(1L)
                .dataCategory(DataCategory.PERSONAL_DATA)
                .purpose(Purpose.PROMOTION)
                .legalBasis("CONSENT")
                .occurredAt(LocalDateTime.now())
                .build();

        projectionService.processEvent(message);
        verify(metricsRepository).save(any(ConsentMetrics.class));
    }

    @Test
    void processEvent_shouldAccumulateMultipleEvents() {
        ConsentEventMessage message1 = ConsentEventMessage.builder()
                .consentId("test-1")
                .eventType("ConsentGranted")
                .dataCategory(DataCategory.PERSONAL_DATA)
                .purpose(Purpose.PROMOTION)
                .occurredAt(LocalDateTime.now())
                .build();

        projectionService.processEvent(message1);

        ArgumentCaptor<ConsentMetrics> captor = ArgumentCaptor.forClass(ConsentMetrics.class);
        verify(metricsRepository).save(captor.capture());

        ConsentMetrics result = captor.getValue();
        assertThat(result.getTotalGranted()).isEqualTo(1);
    }

    @Test
    void processEvent_shouldIgnoreUnknownEventType() {
        ConsentEventMessage message = ConsentEventMessage.builder()
                .consentId("test-consent-1")
                .eventType("UnknownEvent")
                .ownerId(1L)
                .dataCategory(DataCategory.PERSONAL_DATA)
                .purpose(Purpose.PROMOTION)
                .occurredAt(LocalDateTime.now())
                .build();

        projectionService.processEvent(message);

        verify(metricsRepository, never()).save(any());
    }
}