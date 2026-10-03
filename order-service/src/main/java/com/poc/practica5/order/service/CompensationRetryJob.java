package com.poc.practica5.order.service;

import com.poc.practica5.order.client.InventoryClient;
import com.poc.practica5.order.client.PaymentClient;
import com.poc.practica5.order.client.ShippingClient;
import com.poc.practica5.order.entity.OrderStatus;
import com.poc.practica5.order.entity.PendingCompensation;
import com.poc.practica5.order.entity.SagaAction;
import com.poc.practica5.order.entity.SagaResult;
import com.poc.practica5.order.entity.SagaStep;
import com.poc.practica5.order.exception.BusinessException;
import com.poc.practica5.order.repository.OrderRepository;
import com.poc.practica5.order.repository.PendingCompensationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompensationRetryJob {
    private final PendingCompensationRepository pendingCompensationRepository;
    private final OrderRepository orderRepository;
    private final SagaLogService sagaLogService;
    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;
    private final ShippingClient shippingClient;

    @Scheduled(fixedDelay = 15000, initialDelay = 15000)
    public void retryPendingCompensations() {
        List<PendingCompensation> pendings = pendingCompensationRepository.findAllByOrderByCreatedAtAscIdAsc();
        if (pendings.isEmpty()) {
            return;
        }
        log.info("[COMPENSACION] Job: reintentando {} compensaciones pendientes", pendings.size());
        Set<Long> touchedOrders = new LinkedHashSet<>();
        for (PendingCompensation pending : pendings) {
            touchedOrders.add(pending.getOrderId());
            retry(pending);
        }
        touchedOrders.forEach(this::closeOrderIfDone);
    }

    private void retry(PendingCompensation pending) {
        try {
            runCompensation(pending.getStep(), Long.valueOf(pending.getReferenceId()));
            pendingCompensationRepository.delete(pending);
            log.info("[COMPENSACION] Job: {} de la orden {} completada en reintento",
                    pending.getStep(), pending.getOrderId());
            sagaLogService.record(pending.getOrderId(), pending.getStep(), SagaAction.COMPENSATE, SagaResult.SUCCESS,
                    "Compensado por el job de reintento (ref=" + pending.getReferenceId() + ")");
        } catch (BusinessException ex) {
            pendingCompensationRepository.delete(pending);
            sagaLogService.record(pending.getOrderId(), pending.getStep(), SagaAction.COMPENSATE, SagaResult.SUCCESS,
                    "Nada que compensar: " + ex.getMessage());
        } catch (RuntimeException ex) {
            pending.setAttempts(pending.getAttempts() + 1);
            pending.setLastError(ex.getMessage());
            pendingCompensationRepository.save(pending);
            log.warn("[COMPENSACION] Job: {} de la orden {} sigue fallando (intento {}): {}",
                    pending.getStep(), pending.getOrderId(), pending.getAttempts(), ex.getMessage());
        }
    }

    private void runCompensation(SagaStep step, Long referenceId) {
        switch (step) {
            case PAYMENT -> paymentClient.refund(referenceId);
            case INVENTORY -> inventoryClient.release(referenceId);
            case SHIPPING -> shippingClient.cancelShipment(referenceId);
            case ORDER -> {
            }
        }
    }

    private void closeOrderIfDone(Long orderId) {
        if (pendingCompensationRepository.existsByOrderId(orderId)) {
            return;
        }
        orderRepository.findById(orderId)
                .filter(order -> order.getStatus() == OrderStatus.COMPENSATION_PENDING)
                .ifPresent(order -> {
                    order.setStatus(OrderStatus.CANCELLED);
                    orderRepository.save(order);
                    log.info("[COMPENSACION] Job: orden {} sin compensaciones pendientes -> CANCELLED", orderId);
                    sagaLogService.record(orderId, SagaStep.ORDER, SagaAction.COMPENSATE, SagaResult.SUCCESS,
                            "Compensaciones pendientes completadas, orden marcada CANCELLED");
                });
    }
}
