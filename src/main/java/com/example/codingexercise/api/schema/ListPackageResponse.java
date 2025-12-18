package com.example.codingexercise.api.schema;

import lombok.Builder;

import java.util.List;

@Builder
public record ListPackageResponse(int pageNumber, int pageSize, List<PackageResource> packages) {
}
