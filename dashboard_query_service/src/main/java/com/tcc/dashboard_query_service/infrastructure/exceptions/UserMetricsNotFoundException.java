package com.tcc.dashboard_query_service.infrastructure.exceptions;

public class UserMetricsNotFoundException extends RuntimeException {
    public UserMetricsNotFoundException(Long userId) {
        super("Métricas do usuário " + userId + " não encontradas");
    }
}