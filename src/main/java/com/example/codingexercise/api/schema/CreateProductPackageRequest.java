package com.example.codingexercise.api.schema;

import java.util.List;

public class CreateProductPackageRequest {

    private String name;
    private String description;
    private List<String> productIds;

    public CreateProductPackageRequest(String name, String description, List<String> productIds) {
        this.name = name;
        this.description = description;
        this.productIds = productIds;
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
