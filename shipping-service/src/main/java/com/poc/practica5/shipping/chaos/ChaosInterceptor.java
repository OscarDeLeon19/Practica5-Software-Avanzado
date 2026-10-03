package com.poc.practica5.shipping.chaos;

import com.poc.practica5.shipping.exception.ChaosException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChaosInterceptor implements HandlerInterceptor {
    private static final long SLOW_DELAY_MS = 2500;

    private final ChaosService chaosService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws InterruptedException {
        ChaosMode mode = chaosService.getMode();
        String endpoint = request.getMethod() + " " + request.getRequestURI();
        switch (mode) {
            case ERROR -> {
                log.warn("[CHAOS] modo={} endpoint={} -> respondiendo 500", mode, endpoint);
                throw new ChaosException(chaosService.getServiceName());
            }
            case SLOW -> {
                log.warn("[CHAOS] modo={} endpoint={} -> esperando {} ms", mode, endpoint, SLOW_DELAY_MS);
                Thread.sleep(SLOW_DELAY_MS);
            }
            default -> {
            }
        }
        return true;
    }
}
