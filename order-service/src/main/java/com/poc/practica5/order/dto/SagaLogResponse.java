package com.poc.practica5.order.dto;

import com.poc.practica5.order.entity.SagaAction;
import com.poc.practica5.order.entity.SagaLog;
import com.poc.practica5.order.entity.SagaResult;
import com.poc.practica5.order.entity.SagaStep;

import java.time.LocalDateTime;

public record SagaLogResponse(
        Long id,
        Long orderId,
        SagaStep step,
        SagaAction action,
        SagaResult result,
        String message,
        LocalDateTime timestamp) {
    public static SagaLogResponse from(SagaLog log) {
        return new SagaLogResponse(log.getId(), log.getOrderId(), log.getStep(), log.getAction(), log.getResult(),
                log.getMessage(), log.getTimestamp());
    }
}
