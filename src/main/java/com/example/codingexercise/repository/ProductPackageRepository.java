package com.example.codingexercise.repository;

import com.example.codingexercise.model.ProductPackage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ProductPackageRepository {

    private final List<ProductPackage> productPackages = new ArrayList<>();

    public ProductPackage create(String name, String description, List<String> productIds) {
        ProductPackage newProductPackage = new ProductPackage(UUID.randomUUID().toString(), name, description, productIds);
        productPackages.add(newProductPackage);
        return newProductPackage;
    }

    public ProductPackage get(String id) {
        for (ProductPackage p : productPackages) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }
}
