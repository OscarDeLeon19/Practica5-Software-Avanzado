package com.poc.practica5.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Order Service API")
                .version("1.0.0")
                .description("Saga orchestrator of the PoC. Creates orders by coordinating Payment, Inventory and Shipping, compensates failed Sagas and protects remote calls with Resilience4j circuit breakers."));
    }
}
