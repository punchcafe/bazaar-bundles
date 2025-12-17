package com.example.codingexercise.api.schema;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductPackageResource(String id, String name, String description, List<String> productIds) {
    public static ProductPackageResource fromModel(final com.example.codingexercise.model.ProductPackage model) {
        return ProductPackageResource.builder()
                .id(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .productIds(model.getProductIds())
                .build();
    }
}
