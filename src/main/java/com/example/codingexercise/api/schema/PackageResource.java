package com.example.codingexercise.api.schema;

import com.example.codingexercise.model.Package;
import lombok.Builder;

import java.util.List;

@Builder
public record PackageResource(long id, String name, String description, List<String> productIds) {
    public static PackageResource fromModel(final Package model, final List<String> productIds) {
        return PackageResource.builder()
                .id(model.getId())
                .name(model.getName())
                .productIds(productIds)
                .description(model.getDescription())
                .build();
    }
}
