package com.poc.practica5.order.client;

import com.poc.practica5.order.dto.client.ScheduleRequest;
import com.poc.practica5.order.dto.client.ShipmentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ShippingClient extends AbstractRemoteClient {
    private static final String BREAKER = "shippingCB";

    public ShippingClient(RestClientFactory restClientFactory, @Value("${services.shipping.url}") String baseUrl) {
        super("Shipping", restClientFactory.create(baseUrl));
    }

    @CircuitBreaker(name = BREAKER, fallbackMethod = "scheduleFallback")
    public ShipmentResponse schedule(ScheduleRequest request) {
        return call(() -> restClient.post()
                .uri("/shipping/schedule")
                .body(request)
                .retrieve()
                .body(ShipmentResponse.class));
    }

    @Retry(name = "compensation")
    public void cancelShipment(Long shipmentId) {
        log.info("[COMPENSACION] Llamando cancelación del envío {}", shipmentId);
        call(() -> restClient.delete()
                .uri("/shipping/{id}", shipmentId)
                .retrieve()
                .toBodilessEntity());
    }

    private ShipmentResponse scheduleFallback(ScheduleRequest request, Throwable throwable) {
        throw handleFallback(BREAKER, throwable);
    }
}
