package com.poc.practica5.inventory.chaos;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ChaosWebConfig implements WebMvcConfigurer {
    private final ChaosInterceptor chaosInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(chaosInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/admin/**", "/actuator/**", "/error", "/h2-console/**",
                        "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**");
    }
}
