package com.example.codingexercise.api.schema;

import lombok.Builder;

import java.util.List;

@Builder
public record CreateProductPackageRequest(String name, String description, List<String> productIds) {
}
