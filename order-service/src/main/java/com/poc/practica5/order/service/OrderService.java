package com.poc.practica5.order.service;

import com.poc.practica5.order.dto.OrderResponse;
import com.poc.practica5.order.dto.PendingCompensationResponse;
import com.poc.practica5.order.dto.SagaLogResponse;
import com.poc.practica5.order.entity.Order;
import com.poc.practica5.order.entity.OrderStatus;
import com.poc.practica5.order.entity.SagaAction;
import com.poc.practica5.order.entity.SagaResult;
import com.poc.practica5.order.entity.SagaStep;
import com.poc.practica5.order.exception.OrderNotFoundException;
import com.poc.practica5.order.repository.OrderRepository;
import com.poc.practica5.order.repository.PendingCompensationRepository;
import com.poc.practica5.order.repository.SagaLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final SagaLogRepository sagaLogRepository;
    private final PendingCompensationRepository pendingCompensationRepository;
    private final SagaLogService sagaLogService;

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAllByOrderByIdAsc().stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return OrderResponse.from(getOrder(id));
    }

    @Transactional(readOnly = true)
    public List<SagaLogResponse> findSagaLog(Long id) {
        getOrder(id);
        return sagaLogRepository.findByOrderIdOrderByTimestampAscIdAsc(id).stream()
                .map(SagaLogResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PendingCompensationResponse> findPendingCompensations() {
        return pendingCompensationRepository.findAllByOrderByCreatedAtAscIdAsc().stream()
                .map(PendingCompensationResponse::from)
                .toList();
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        Order order = getOrder(id);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.info("[COMPENSACION] Orden {} ya estaba CANCELLED (idempotente)", id);
            return OrderResponse.from(order);
        }
        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.saveAndFlush(order);
        log.info("[COMPENSACION] Orden {} marcada CANCELLED vía DELETE /orders/{}", id, id);
        sagaLogService.record(id, SagaStep.ORDER, SagaAction.COMPENSATE, SagaResult.SUCCESS,
                "Orden cancelada vía DELETE /orders/" + id);
        return OrderResponse.from(saved);
    }

    private Order getOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}
