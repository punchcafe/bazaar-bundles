package com.example.codingexercise.api.schema;

import lombok.Builder;

import java.util.List;

@Builder
public record CreatePackageRequest(String name, String description) {
}
