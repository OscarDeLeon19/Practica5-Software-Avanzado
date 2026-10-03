package com.poc.practica5.order.client;

import com.poc.practica5.order.dto.client.RemoteError;
import com.poc.practica5.order.exception.BusinessException;
import com.poc.practica5.order.exception.OutOfStockException;
import com.poc.practica5.order.exception.PaymentRejectedException;
import com.poc.practica5.order.exception.RemoteServiceException;
import com.poc.practica5.order.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.function.Supplier;

@Slf4j
public abstract class AbstractRemoteClient {
    private final String serviceName;
    protected final RestClient restClient;

    protected AbstractRemoteClient(String serviceName, RestClient restClient) {
        this.serviceName = serviceName;
        this.restClient = restClient;
    }

    protected <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (HttpClientErrorException ex) {
            String message = extractMessage(ex);
            if (ex.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
                throw new OutOfStockException(message);
            }
            if (ex.getStatusCode().value() == HttpStatus.PAYMENT_REQUIRED.value()) {
                throw new PaymentRejectedException(message);
            }
            throw new BusinessException("HTTP_" + ex.getStatusCode().value(), message);
        } catch (HttpServerErrorException ex) {
            throw new RemoteServiceException("HTTP " + ex.getStatusCode().value() + " - " + extractMessage(ex), ex);
        } catch (ResourceAccessException ex) {
            throw new RemoteServiceException("sin respuesta (" + rootCauseMessage(ex) + ")", ex);
        } catch (RestClientException ex) {
            throw new RemoteServiceException(ex.getMessage(), ex);
        }
    }

    protected RuntimeException handleFallback(String breakerName, Throwable throwable) {
        if (throwable instanceof BusinessException business) {
            return business;
        }

        if (throwable instanceof CallNotPermittedException) {
            log.warn("[CB] {} OPEN, petición rechazada sin llamar al servicio", breakerName);
            return new ServiceUnavailableException(
                    "Servicio " + serviceName + " no disponible (circuito abierto), intente más tarde", throwable);
        }

        log.warn("[CB] {} registró fallo técnico: {}", breakerName, throwable.getMessage());
        return new ServiceUnavailableException(
                "Servicio " + serviceName + " falló: " + throwable.getMessage() + ". Intente más tarde", throwable);
    }

    private String extractMessage(HttpStatusCodeException ex) {
        try {
            RemoteError error = ex.getResponseBodyAs(RemoteError.class);
            if (error != null && error.message() != null) {
                return error.message();
            }
        } catch (RuntimeException ignored) {
        }
        String body = ex.getResponseBodyAsString();
        return body.isBlank() ? ex.getStatusText() : body;
    }

    private String rootCauseMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getClass().getSimpleName() + (root.getMessage() != null ? ": " + root.getMessage() : "");
    }
}
