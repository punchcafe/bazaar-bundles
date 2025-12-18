package com.example.codingexercise.gateway;

import com.example.codingexercise.gateway.dto.Product;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductServiceGateway {

    private final RestTemplate restTemplate;

    public ProductServiceGateway(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Product getProduct(String id) {
        return restTemplate.getForObject("https://product-service.herokuapp.com/api/v1/products/{id}", Product.class, id);
    }
}
