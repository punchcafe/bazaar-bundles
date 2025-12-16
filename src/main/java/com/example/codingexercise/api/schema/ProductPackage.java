package com.example.codingexercise.api.schema;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductPackage(String id, String name, String description, List<String> productIds) {
    public static ProductPackage fromModel(final com.example.codingexercise.model.ProductPackage model) {
        return ProductPackage.builder()
                .id(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .productIds(model.getProductIds())
                .build();
    }
}
