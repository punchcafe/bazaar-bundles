package com.example.codingexercise;

import com.example.codingexercise.api.schema.CreateProductPackageRequest;
import com.example.codingexercise.api.schema.ProductPackageResource;
import com.example.codingexercise.model.ProductPackage;
import com.example.codingexercise.repository.PackageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PackageControllerTests {

    private final static String TEST_PRODUCT_NAME = "Test Name";
    private final static String TEST_PRODUCT_DESCRIPTION = "Test Description";
    private final static List<String> TEST_PRODUCT_PRODUCT_LIST = List.of("prod1");

	private final TestRestTemplate restTemplate;
    private final PackageRepository packageRepository;

    @Autowired
    PackageControllerTests(TestRestTemplate restTemplate, PackageRepository packageRepository) {
		this.restTemplate = restTemplate;
        this.packageRepository = packageRepository;
    }

    @Test
    void createPackage_returns200AndCreatedPackage() {
        // Arrange
        final var request = CreateProductPackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(TEST_PRODUCT_PRODUCT_LIST)
                .build();

        // Act
		ResponseEntity<ProductPackageResource> response = POST_productPackage(request);
        ProductPackageResource responseBody = response.getBody();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Unexpected status code");
        assertNotNull(responseBody);
        assertEquals(TEST_PRODUCT_NAME, responseBody.name());
        assertEquals(TEST_PRODUCT_DESCRIPTION, responseBody.description());
        assertEquals(TEST_PRODUCT_PRODUCT_LIST, responseBody.productIds());
    }

    @Test
    void createPackage_andGetPackage_returns200AndCreatedPackage() {
        // Arrange
        final var request = CreateProductPackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(TEST_PRODUCT_PRODUCT_LIST)
                .build();

        // Act
        ResponseEntity<ProductPackageResource> response = POST_productPackage(request);
        ProductPackageResource createdBody = response.getBody();
        final var createdId = createdBody.id();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        final var getProductPackageResponse = GET_productPackage(createdId);
        assertEquals(HttpStatus.OK, getProductPackageResponse.getStatusCode());
        assertEquals(createdBody, getProductPackageResponse.getBody());
    }

    @Test
    void getPackage_Returns200AndEntityWhenPackageExists() {
        // Arrange
        final var existingPackage = provisionProductPackage("Test Name 2", "Test Desc 2", List.of("prod2"));

        // Act
        final var response = GET_productPackage(existingPackage.id());
        final var responseBody = response.getBody();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals(existingPackage.id(), responseBody.id());
        assertEquals(existingPackage.name(), responseBody.name());
        assertEquals(existingPackage.description(), responseBody.description());
        assertEquals(existingPackage.productIds(), responseBody.productIds());
    }

    private ResponseEntity<ProductPackageResource> GET_productPackage(final String id){
        return restTemplate.getForEntity("/packages/{id}", ProductPackageResource.class, id);
    }

    private ResponseEntity<ProductPackageResource> POST_productPackage(final CreateProductPackageRequest request){
        return restTemplate.postForEntity("/packages", request, ProductPackageResource.class);
    }

    private ProductPackageResource provisionProductPackage(final String name, final String description, final List<String> productIds) {
        ProductPackage productPackage = packageRepository.create(name, description, productIds);
        return ProductPackageResource.fromModel(productPackage);
    }

}
