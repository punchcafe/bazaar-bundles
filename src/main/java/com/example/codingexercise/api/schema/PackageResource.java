package com.example.codingexercise.api.schema;

import com.example.codingexercise.packages.Package;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record PackageResource(long id, String name, String description, List<String> productIds, int totalPrice) { }
