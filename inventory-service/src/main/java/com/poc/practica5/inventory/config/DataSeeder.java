package com.poc.practica5.inventory.config;

import com.poc.practica5.inventory.entity.Product;
import com.poc.practica5.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }
        productRepository.saveAll(List.of(
                Product.builder().id("P-001").name("Laptop").stock(100).build(),
                Product.builder().id("P-002").name("Monitor").stock(0).build(),
                Product.builder().id("P-003").name("Teclado").stock(5).build()));
        log.info("Productos semilla cargados: P-001 (100), P-002 (0), P-003 (5)");
    }
}
