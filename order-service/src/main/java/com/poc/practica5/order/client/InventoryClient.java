package com.poc.practica5.order.client;

import com.poc.practica5.order.dto.client.ReleaseRequest;
import com.poc.practica5.order.dto.client.ReservationResponse;
import com.poc.practica5.order.dto.client.ReserveRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryClient extends AbstractRemoteClient {
    private static final String BREAKER = "inventoryCB";

    public InventoryClient(RestClientFactory restClientFactory, @Value("${services.inventory.url}") String baseUrl) {
        super("Inventory", restClientFactory.create(baseUrl));
    }

    @CircuitBreaker(name = BREAKER, fallbackMethod = "reserveFallback")
    public ReservationResponse reserve(ReserveRequest request) {
        return call(() -> restClient.post()
                .uri("/inventory/reserve")
                .body(request)
                .retrieve()
                .body(ReservationResponse.class));
    }

    @Retry(name = "compensation")
    public void release(Long orderId) {
        log.info("[COMPENSACION] Llamando release del inventario de la orden {}", orderId);
        call(() -> restClient.post()
                .uri("/inventory/release")
                .body(new ReleaseRequest(orderId))
                .retrieve()
                .toBodilessEntity());
    }

    private ReservationResponse reserveFallback(ReserveRequest request, Throwable throwable) {
        throw handleFallback(BREAKER, throwable);
    }
}
