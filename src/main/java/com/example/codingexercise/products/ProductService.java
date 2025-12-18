package com.example.codingexercise.products;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductService {
    final ProductsCache productsCache;

    public ProductService(final ProductsCache cache){
        this.productsCache = cache;
    }

    public Optional<Product> lookup(final String productId) {
        return Optional.ofNullable(this.productsCache.getCache().get(productId));
    }

    public List<Product> all() {
        return this.productsCache.getCache().values().stream().toList();
    }
}
