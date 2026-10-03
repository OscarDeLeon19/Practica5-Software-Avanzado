package com.poc.practica5.order.service;

import com.poc.practica5.order.entity.SagaAction;
import com.poc.practica5.order.entity.SagaLog;
import com.poc.practica5.order.entity.SagaResult;
import com.poc.practica5.order.entity.SagaStep;
import com.poc.practica5.order.repository.SagaLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SagaLogService {
    private final SagaLogRepository sagaLogRepository;

    public void record(Long orderId, SagaStep step, SagaAction action, SagaResult result, String message) {
        sagaLogRepository.save(SagaLog.builder()
                .orderId(orderId)
                .step(step)
                .action(action)
                .result(result)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
