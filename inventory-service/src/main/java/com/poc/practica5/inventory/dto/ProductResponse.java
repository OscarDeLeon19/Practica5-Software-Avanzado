package com.poc.practica5.inventory.dto;

import com.poc.practica5.inventory.entity.Product;

public record ProductResponse(String id, String name, int stock) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getStock());
    }
}
