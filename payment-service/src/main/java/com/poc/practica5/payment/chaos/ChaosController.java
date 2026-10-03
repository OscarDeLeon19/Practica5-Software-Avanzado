package com.poc.practica5.payment.chaos;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chaos", description = "Failure simulation for the business endpoints")
@RestController
@RequestMapping("/admin/chaos")
@RequiredArgsConstructor
public class ChaosController {
    private final ChaosService chaosService;

    @Operation(summary = "Change the chaos mode",
            description = "OK: normal behaviour. ERROR: business endpoints respond 500. "
                    + "SLOW: business endpoints wait 2.5 s before responding. ERROR_ON_REFUND: only the refund endpoint responds 500.")
    @PostMapping
    public ChaosResponse setMode(@Parameter(description = "New chaos mode", example = "ERROR") @RequestParam ChaosMode mode) {
        return new ChaosResponse(chaosService.getServiceName(), chaosService.setMode(mode));
    }

    @Operation(summary = "Get the current chaos mode")
    @GetMapping
    public ChaosResponse getMode() {
        return new ChaosResponse(chaosService.getServiceName(), chaosService.getMode());
    }
}
