package com.example.codingexercise.api.schema;

import java.util.List;

public class ProductPackage {
    public static ProductPackage fromModel(final com.example.codingexercise.model.ProductPackage model) {
        return new ProductPackage(model.getId(), model.getName(), model.getDescription(), model.getProductIds());
    }

    private String id;
    private String name;
    private String description;
    private List<String> productIds;

    public ProductPackage(String id, String name, String description, List<String> productIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.productIds = productIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<String> productIds) {
        this.productIds = productIds;
    }
}
