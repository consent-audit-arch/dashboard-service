package com.tcc.dashboard_query_service.infrastructure.messaging.DTOs;

import com.tcc.dashboard_query_service.model.metrics.enums.DataCategory;
import com.tcc.dashboard_query_service.model.metrics.enums.Purpose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentEventMessage {
    private String eventId;
    private String eventType;
    private String consentId;
    private Long ownerId;
    private Long userId;
    private DataCategory dataCategory;
    private Purpose purpose;
    private String legalBasis;
    private LocalDateTime occurredAt;
    private IssuedByMessage issuedBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssuedByMessage {
        private Long id;
        private String issuer;
    }

    public boolean isConsentGranted() {
        return "ConsentGranted".equals(eventType);
    }

    public boolean isConsentRevoked() {
        return "ConsentRevoked".equals(eventType);
    }
}