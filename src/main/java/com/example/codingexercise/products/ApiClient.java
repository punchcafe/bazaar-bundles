package com.example.codingexercise.products;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

class ApiClient {

    @Cacheable("products")
    public List<Product> allProducts(){
        return List.of();
    }

    @CacheEvict("products")
    public void cacheEvict(){

    }
}
