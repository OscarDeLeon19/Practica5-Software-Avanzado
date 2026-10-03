package com.poc.practica5.shipping.chaos;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class ChaosService {
    private final AtomicReference<ChaosMode> mode;

    @Getter
    private final String serviceName;

    public ChaosService(@Value("${chaos.mode:OK}") ChaosMode initialMode,
                        @Value("${spring.application.name}") String serviceName) {
        this.mode = new AtomicReference<>(initialMode);
        this.serviceName = serviceName;
        log.info("[CHAOS] {} iniciado con modo={}", serviceName, initialMode);
    }

    public ChaosMode getMode() {
        return mode.get();
    }

    public ChaosMode setMode(ChaosMode newMode) {
        ChaosMode previous = mode.getAndSet(newMode);
        log.warn("[CHAOS] {} cambio de modo: {} -> {}", serviceName, previous, newMode);
        return newMode;
    }
}
