package com.poc.practica5.payment.service;

import com.poc.practica5.payment.dto.ChargeRequest;
import com.poc.practica5.payment.dto.PaymentResponse;
import com.poc.practica5.payment.entity.Payment;
import com.poc.practica5.payment.entity.PaymentStatus;
import com.poc.practica5.payment.exception.NotFoundException;
import com.poc.practica5.payment.exception.PaymentRejectedException;
import com.poc.practica5.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000");

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse charge(ChargeRequest request) {
        if (request.amount().compareTo(MAX_AMOUNT) > 0) {
            log.warn("Pago rechazado para la orden {}: monto {} supera el límite {}",
                    request.orderId(), request.amount(), MAX_AMOUNT);
            throw new PaymentRejectedException();
        }
        Payment payment = paymentRepository.save(Payment.builder()
                .orderId(request.orderId())
                .amount(request.amount())
                .status(PaymentStatus.CHARGED)
                .build());
        log.info("Pago {} CHARGED para la orden {} por {}", payment.getId(), payment.getOrderId(), payment.getAmount());
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse refund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Pago " + paymentId + " no encontrado"));
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            log.info("[COMPENSACION] Pago {} ya estaba REFUNDED, no se hace nada (idempotente)", paymentId);
            return PaymentResponse.from(payment);
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        Payment saved = paymentRepository.saveAndFlush(payment);
        log.info("[COMPENSACION] Pago {} de la orden {} REFUNDED", paymentId, payment.getOrderId());
        return PaymentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findAll() {
        return paymentRepository.findAll().stream().map(PaymentResponse::from).toList();
    }
}
