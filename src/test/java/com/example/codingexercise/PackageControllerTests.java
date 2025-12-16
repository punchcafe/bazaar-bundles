package com.example.codingexercise;

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

	private final TestRestTemplate restTemplate;
    private final PackageRepository packageRepository;

    @Autowired
    PackageControllerTests(TestRestTemplate restTemplate, PackageRepository packageRepository) {
		this.restTemplate = restTemplate;
        this.packageRepository = packageRepository;
    }

    @Test
    void createPackage() {
		ResponseEntity<ProductPackage> created = restTemplate.postForEntity("/packages", new ProductPackage(null, "Test Name", "Test Desc", List.of("prod1")), ProductPackage.class);
        assertEquals(HttpStatus.OK, created.getStatusCode(), "Unexpected status code");
        ProductPackage createdBody = created.getBody();
        assertNotNull(createdBody, "Unexpected body");
        assertEquals("Test Name", createdBody.getName(), "Unexpected name");
        assertEquals("Test Desc", createdBody.getDescription(), "Unexpected description");
        assertEquals(List.of("prod1"), createdBody.getProductIds(), "Unexpected products");

        ProductPackage productPackage = packageRepository.get(createdBody.getId());
        assertNotNull(productPackage, "Unexpected package");
        assertEquals(createdBody.getId(), productPackage.getId(), "Unexpected id");
        assertEquals(createdBody.getName(), productPackage.getName(), "Unexpected name");
        assertEquals(createdBody.getDescription(), productPackage.getDescription(), "Unexpected description");
        assertEquals(createdBody.getProductIds(), productPackage.getProductIds(), "Unexpected products");
    }

    @Test
    void getPackage() {
        ProductPackage productPackage = packageRepository.create("Test Name 2", "Test Desc 2", List.of("prod2"));
        ResponseEntity<ProductPackage> fetched = restTemplate.getForEntity("/packages/{id}", ProductPackage.class, productPackage.getId());
        assertEquals(HttpStatus.OK, fetched.getStatusCode(), "Unexpected status code");
        ProductPackage fetchedBody = fetched.getBody();
        assertNotNull(fetchedBody, "Unexpected body");
        assertEquals(productPackage.getId(), fetchedBody.getId(), "Unexpected id");
        assertEquals(productPackage.getName(), fetchedBody.getName(), "Unexpected name");
        assertEquals(productPackage.getDescription(), fetchedBody.getDescription(), "Unexpected description");
        assertEquals(productPackage.getProductIds(), fetchedBody.getProductIds(), "Unexpected products");
    }

    @Test
    void listPackages() {
        ProductPackage productPackage1 = packageRepository.create("Test Name 1", "Test Desc 1", List.of("prod1"));
        ProductPackage productPackage2 = packageRepository.create("Test Name 2", "Test Desc 2", List.of("prod2"));

        ResponseEntity<Object> fetched = restTemplate.getForEntity("/packages", Object.class);
        assertEquals(HttpStatus.OK, fetched.getStatusCode(), "Unexpected status code");
    }

}
