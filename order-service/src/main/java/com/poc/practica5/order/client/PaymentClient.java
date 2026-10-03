package com.poc.practica5.order.client;

import com.poc.practica5.order.dto.client.ChargeRequest;
import com.poc.practica5.order.dto.client.PaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentClient extends AbstractRemoteClient {
    private static final String BREAKER = "paymentCB";

    public PaymentClient(RestClientFactory restClientFactory, @Value("${services.payment.url}") String baseUrl) {
        super("Payment", restClientFactory.create(baseUrl));
    }

    @CircuitBreaker(name = BREAKER, fallbackMethod = "chargeFallback")
    public PaymentResponse charge(ChargeRequest request) {
        return call(() -> restClient.post()
                .uri("/payments")
                .body(request)
                .retrieve()
                .body(PaymentResponse.class));
    }

    @Retry(name = "compensation")
    public void refund(Long paymentId) {
        log.info("[COMPENSACION] Llamando refund del pago {}", paymentId);
        call(() -> restClient.post()
                .uri("/payments/{id}/refund", paymentId)
                .retrieve()
                .toBodilessEntity());
    }

    private PaymentResponse chargeFallback(ChargeRequest request, Throwable throwable) {
        throw handleFallback(BREAKER, throwable);
    }
}
