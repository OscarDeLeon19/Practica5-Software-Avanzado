package com.poc.practica5.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Inventory Service API")
                .version("1.0.0")
                .description("Saga participant that reserves and releases product stock. Includes chaos endpoints to simulate failures."));
    }
}
