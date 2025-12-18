package com.example.codingexercise.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationPropertiesScan
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "products-api")
@Data
public class ProductsApiConfiguration {
    private String username;
    private String password;
}
