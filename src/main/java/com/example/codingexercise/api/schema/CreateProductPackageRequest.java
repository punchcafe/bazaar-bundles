package com.example.codingexercise.api.schema;

import java.util.List;

public record CreateProductPackageRequest(String name, String description, List<String> productIds) {
}
