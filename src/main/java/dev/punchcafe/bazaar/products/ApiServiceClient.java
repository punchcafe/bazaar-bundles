package dev.punchcafe.bazaar.products;

import dev.punchcafe.bazaar.config.ProductsApiConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class ApiServiceClient {
    private final RestTemplate restTemplate;

    public ApiServiceClient(RestTemplateBuilder builder, ProductsApiConfiguration configuration) {
        this.restTemplate = builder.basicAuthentication(configuration.getUsername(), configuration.getPassword()).build();
    }

    public List<Product> getAllProducts() {
        // TODO: extract to config
        final var entities = restTemplate.getForEntity(
                "https://product-service.herokuapp.com/api/v1/products",
                Product[].class
        );
        return Arrays.asList(entities.getBody());
    }
}
