package com.example.codingexercise.products;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductsService {

    private Map<String, Product> products = new HashMap<>();

    List<String> invalidIds(List<String> ids)
    {
        return List.of();
    }

    List<Product> retrieveProducts(List<String> productIds){}
}
