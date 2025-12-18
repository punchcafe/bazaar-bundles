package com.example.codingexercise.products;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO: determine how to keep package privacy and still mock.
@Component
public class ProductsCache {

    final ApiServiceClient client;

    public ProductsCache(final ApiServiceClient client){
        this.client = client;
    }

    @Cacheable
    public Map<String, Product> getCache() {
        return this.client.getAllProducts().stream().collect(Collectors.toMap(Product::id, Function.identity()));
    }

    // TODO: add metrics and alerting for this
    @Scheduled(cron = "*/5 * * * *")
    @CacheEvict
    void evictCache() {}

}
