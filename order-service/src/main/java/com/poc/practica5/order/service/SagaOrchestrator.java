package com.poc.practica5.order.service;

import com.poc.practica5.order.client.InventoryClient;
import com.poc.practica5.order.client.PaymentClient;
import com.poc.practica5.order.client.ShippingClient;
import com.poc.practica5.order.dto.OrderRequest;
import com.poc.practica5.order.dto.client.ChargeRequest;
import com.poc.practica5.order.dto.client.PaymentResponse;
import com.poc.practica5.order.dto.client.ReservationResponse;
import com.poc.practica5.order.dto.client.ReserveRequest;
import com.poc.practica5.order.dto.client.ScheduleRequest;
import com.poc.practica5.order.dto.client.ShipmentResponse;
import com.poc.practica5.order.entity.Order;
import com.poc.practica5.order.entity.OrderStatus;
import com.poc.practica5.order.entity.PendingCompensation;
import com.poc.practica5.order.entity.SagaAction;
import com.poc.practica5.order.entity.SagaResult;
import com.poc.practica5.order.entity.SagaStep;
import com.poc.practica5.order.exception.BusinessException;
import com.poc.practica5.order.exception.SagaFailedException;
import com.poc.practica5.order.repository.OrderRepository;
import com.poc.practica5.order.repository.PendingCompensationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrchestrator {
    private final OrderRepository orderRepository;
    private final PendingCompensationRepository pendingCompensationRepository;
    private final SagaLogService sagaLogService;
    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;
    private final ShippingClient shippingClient;

    public Order execute(OrderRequest request) {
        Order order = orderRepository.save(Order.builder()
                .customerId(request.customerId())
                .productId(request.productId())
                .quantity(request.quantity())
                .amount(request.amount())
                .address(request.address())
                .status(OrderStatus.PENDING)
                .build());
        Long orderId = order.getId();
        log.info("[SAGA] Paso 1/4 ORDER creada id={} cliente={} producto={} cantidad={} monto={}",
                orderId, request.customerId(), request.productId(), request.quantity(), request.amount());
        sagaLogService.record(orderId, SagaStep.ORDER, SagaAction.EXECUTE, SagaResult.SUCCESS,
                "Orden creada en estado PENDING");

        Deque<CompensableStep> completedSteps = new ArrayDeque<>();
        completedSteps.push(new CompensableStep(SagaStep.ORDER, String.valueOf(orderId), () -> closeOrder(order)));

        SagaStep currentStep = SagaStep.PAYMENT;
        try {
            PaymentResponse payment = paymentClient.charge(new ChargeRequest(orderId, request.amount()));
            order.setPaymentId(payment.paymentId());
            orderRepository.save(order);
            log.info("[SAGA] Paso 2/4 PAYMENT cobrado paymentId={} monto={}", payment.paymentId(), payment.amount());
            sagaLogService.record(orderId, SagaStep.PAYMENT, SagaAction.EXECUTE, SagaResult.SUCCESS,
                    "Pago " + payment.paymentId() + " CHARGED");
            completedSteps.push(new CompensableStep(SagaStep.PAYMENT, String.valueOf(payment.paymentId()),
                    () -> paymentClient.refund(payment.paymentId())));

            currentStep = SagaStep.INVENTORY;
            ReservationResponse reservation = inventoryClient.reserve(
                    new ReserveRequest(orderId, request.productId(), request.quantity()));
            log.info("[SAGA] Paso 3/4 INVENTORY reservado reservationId={} producto={} cantidad={}",
                    reservation.reservationId(), request.productId(), request.quantity());
            sagaLogService.record(orderId, SagaStep.INVENTORY, SagaAction.EXECUTE, SagaResult.SUCCESS,
                    "Reserva " + reservation.reservationId() + " de " + request.quantity() + " x " + request.productId());
            completedSteps.push(new CompensableStep(SagaStep.INVENTORY, String.valueOf(orderId),
                    () -> inventoryClient.release(orderId)));

            currentStep = SagaStep.SHIPPING;
            ShipmentResponse shipment = shippingClient.schedule(new ScheduleRequest(orderId, request.address()));
            order.setShipmentId(shipment.shipmentId());
            log.info("[SAGA] Paso 4/4 SHIPPING programado shipmentId={} fecha={}",
                    shipment.shipmentId(), shipment.scheduledDate());
            sagaLogService.record(orderId, SagaStep.SHIPPING, SagaAction.EXECUTE, SagaResult.SUCCESS,
                    "Envío " + shipment.shipmentId() + " SCHEDULED para " + shipment.scheduledDate());
            completedSteps.push(new CompensableStep(SagaStep.SHIPPING, String.valueOf(shipment.shipmentId()),
                    () -> shippingClient.cancelShipment(shipment.shipmentId())));

            order.setStatus(OrderStatus.COMPLETED);
            Order completed = orderRepository.save(order);
            log.info("[SAGA] Orden {} COMPLETED", orderId);
            return completed;
        } catch (RuntimeException ex) {
            log.error("[SAGA] Paso {} falló para la orden {}: {}", currentStep, orderId, ex.getMessage());
            sagaLogService.record(orderId, currentStep, SagaAction.EXECUTE, SagaResult.FAILED, ex.getMessage());
            compensate(order, completedSteps, ex.getMessage());
            throw new SagaFailedException(orderRepository.findById(orderId).orElse(order), ex);
        }
    }

    private void compensate(Order order, Deque<CompensableStep> completedSteps, String reason) {
        Long orderId = order.getId();
        order.setFailureReason(reason);
        log.warn("[COMPENSACION] Iniciando compensaciones de la orden {} ({} pasos a deshacer)",
                orderId, completedSteps.size());
        while (!completedSteps.isEmpty()) {
            CompensableStep step = completedSteps.pop();
            try {
                step.compensation().run();
                String message = step.step() == SagaStep.ORDER
                        ? "Orden marcada " + order.getStatus()
                        : "Compensado (ref=" + step.referenceId() + ")";
                log.info("[COMPENSACION] {} -> {}", step.step(), message);
                sagaLogService.record(orderId, step.step(), SagaAction.COMPENSATE, SagaResult.SUCCESS, message);
            } catch (BusinessException ex) {
                log.info("[COMPENSACION] {} sin nada que compensar: {}", step.step(), ex.getMessage());
                sagaLogService.record(orderId, step.step(), SagaAction.COMPENSATE, SagaResult.SUCCESS,
                        "Nada que compensar: " + ex.getMessage());
            } catch (RuntimeException ex) {
                enqueue(order, step, ex);
            }
        }
    }

    private void closeOrder(Order order) {
        if (order.getStatus() != OrderStatus.COMPENSATION_PENDING) {
            order.setStatus(OrderStatus.CANCELLED);
        }
        orderRepository.save(order);
    }

    private void enqueue(Order order, CompensableStep step, RuntimeException ex) {
        pendingCompensationRepository.save(PendingCompensation.builder()
                .orderId(order.getId())
                .step(step.step())
                .referenceId(step.referenceId())
                .attempts(1)
                .lastError(ex.getMessage())
                .build());
        order.setStatus(OrderStatus.COMPENSATION_PENDING);
        orderRepository.save(order);
        log.error("[COMPENSACION] {} de la orden {} falló tras los reintentos ({}): encolada para reintento",
                step.step(), order.getId(), ex.getMessage());
        sagaLogService.record(order.getId(), step.step(), SagaAction.COMPENSATE, SagaResult.FAILED,
                "Encolada para reintento: " + ex.getMessage());
    }
}
