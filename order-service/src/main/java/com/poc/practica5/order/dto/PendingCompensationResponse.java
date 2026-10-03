package com.poc.practica5.order.dto;

import com.poc.practica5.order.entity.PendingCompensation;
import com.poc.practica5.order.entity.SagaStep;

import java.time.LocalDateTime;

public record PendingCompensationResponse(
        Long id,
        Long orderId,
        SagaStep step,
        String referenceId,
        int attempts,
        String lastError,
        LocalDateTime createdAt) {
    public static PendingCompensationResponse from(PendingCompensation pending) {
        return new PendingCompensationResponse(pending.getId(), pending.getOrderId(), pending.getStep(),
                pending.getReferenceId(), pending.getAttempts(), pending.getLastError(), pending.getCreatedAt());
    }
}
