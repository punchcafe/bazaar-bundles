package com.example.codingexercise;

import com.example.codingexercise.api.schema.ChangePackageRequest;
import com.example.codingexercise.api.schema.ErrorResponse;
import com.example.codingexercise.api.schema.PackageResource;
import com.example.codingexercise.model.Package;
import com.example.codingexercise.repository.PackageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PackageControllerTests {

    private final static String TEST_PRODUCT_NAME = "Test Name";
    private final static String TEST_PRODUCT_DESCRIPTION = "Test Description";

    private final static String UPDATED_PRODUCT_NAME = "Updated Test Name";
    private final static String UPDATED_PRODUCT_DESCRIPTION = "Updated Test Description";

	private final TestRestTemplate restTemplate;
    private final PackageRepository packageRepository;

    @Autowired
    PackageControllerTests(TestRestTemplate restTemplate, PackageRepository packageRepository) {
		this.restTemplate = restTemplate;
        this.packageRepository = packageRepository;
    }

    // TODO: add validations for invalid and null parameters

    @Test
    void createPackage_returns201AndCreatedPackage() {
        // Arrange
        final var request = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of())
                .build();

        // Act
		ResponseEntity<PackageResource> response = POST_productPackage(request);
        PackageResource responseBody = response.getBody();

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Unexpected status code");
        assertNotNull(responseBody);
        assertEquals(TEST_PRODUCT_NAME, responseBody.name());
        assertEquals(List.of(), responseBody.productIds());
        assertEquals(TEST_PRODUCT_DESCRIPTION, responseBody.description());
    }

    @Test
    void createPackage_returnsPersistedProductIdsInResponse() {
        // Arrange
        final var request = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of("a", "b", "c"))
                .build();

        // Act
        ResponseEntity<PackageResource> response = POST_productPackage(request);
        PackageResource responseBody = response.getBody();

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Unexpected status code");
        assertEquals(List.of("a", "b", "c"), responseBody.productIds());
    }

    @Test
    void createPackage_persistsProductIds() {
        // Arrange
        final var request = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of("a", "b", "c", "d"))
                .build();

        // Act
        ResponseEntity<PackageResource> response = POST_productPackage(request);
        PackageResource responseBody = response.getBody();

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Unexpected status code");
        assertEquals(List.of("a", "b", "c", "d"), responseBody.productIds());
    }

    @Test
    void createPackageWithProductIds_andGetPackage_returnsCreatedProductIds() {
        // Arrange
        final var request = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of("hello", "world"))
                .build();

        // Act
        ResponseEntity<PackageResource> response = POST_productPackage(request);
        PackageResource createdBody = response.getBody();
        final var createdId = createdBody.id();
        final var getProductPackageResponse = GET_productPackage(createdId);

        // Assert
        assertEquals(HttpStatus.OK, getProductPackageResponse.getStatusCode());
        assertEquals(List.of("hello", "world"), getProductPackageResponse.getBody().productIds());
    }

    @Test
    void createPackage_andGetPackage_returnsCreatedPackage() {
        // Arrange
        final var request = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of())
                .build();

        // Act
        ResponseEntity<PackageResource> response = POST_productPackage(request);
        PackageResource createdBody = response.getBody();
        final var createdId = createdBody.id();
        final var getProductPackageResponse = GET_productPackage(createdId);

        // Assert
        assertEquals(HttpStatus.OK, getProductPackageResponse.getStatusCode());
        assertEquals(createdBody, getProductPackageResponse.getBody());
    }

    @Test
    void getPackage_Returns200AndEntityWhenPackageExists() {
        // Arrange
        final var existingPackage = provisionProductPackage("Test Name 2", "Test Desc 2");

        // Act
        final var response = GET_productPackage(existingPackage.id());
        final var responseBody = response.getBody();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals(existingPackage.id(), responseBody.id());
        assertEquals(existingPackage.name(), responseBody.name());
        assertEquals(existingPackage.description(), responseBody.description());
    }

    @Test
    void getPackage_Returns404AndErrorMessageWhenPackageDoesntExist() {
        // Act
        final var response = GET_productPackage(101202303, ErrorResponse.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(new ErrorResponse("package not found"), response.getBody());
    }

    @Test
    void getPackage_Returns400AndErrorMessageWhenInvalidID() {
        // Act
        final var response = GET_productPackage("invalid_value", ErrorResponse.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(new ErrorResponse("invalid id: must be a number"), response.getBody());
    }

    @Test
    void updatePackage_returns200AndUpdatedPackage() {
        // Arrange
        final var createRequest = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of("a", "b"))
                .build();

        ResponseEntity<PackageResource> creationResponse = POST_productPackage(createRequest);
        PackageResource createdEntity = creationResponse.getBody();

        final var request = ChangePackageRequest.builder()
                .name(UPDATED_PRODUCT_NAME)
                .description(UPDATED_PRODUCT_DESCRIPTION)
                .productIds(List.of("c", "d"))
                .build();

        // Act
        final var response = PUT_productPackage(createdEntity.id(), request);
        final var updatedEntity = response.getBody();


        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(UPDATED_PRODUCT_NAME, updatedEntity.name());
        assertEquals(UPDATED_PRODUCT_DESCRIPTION, updatedEntity.description());
        assertEquals(List.of("c", "d"), updatedEntity.productIds());
    }


    @Test
    void updatePackage_canSetProductsToEmpty() {
        // Arrange
        final var createRequest = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of("a", "b"))
                .build();

        ResponseEntity<PackageResource> creationResponse = POST_productPackage(createRequest);
        PackageResource createdEntity = creationResponse.getBody();

        final var request = ChangePackageRequest.builder()
                .productIds(List.of())
                .build();

        // Act
        final var response = PUT_productPackage(createdEntity.id(), request);
        final var updatedEntity = response.getBody();


        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(List.of(), updatedEntity.productIds());
    }

    @Test
    void updatePackage_keepsAnyExistingProductIdsInRequest() {
        // Arrange
        final var createRequest = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of("a", "b"))
                .build();

        ResponseEntity<PackageResource> creationResponse = POST_productPackage(createRequest);
        PackageResource createdEntity = creationResponse.getBody();

        final var request = ChangePackageRequest.builder()
                .productIds(List.of("b", "c"))
                .build();

        // Act
        final var response = PUT_productPackage(createdEntity.id(), request);
        final var updatedEntity = response.getBody();


        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(List.of("b", "c"), updatedEntity.productIds());
    }

    @Test
    void updatePackage_returns404ifPackageDoesntExist() {
        // Arrange
        final var request = ChangePackageRequest.builder()
                .name(UPDATED_PRODUCT_NAME)
                .description(UPDATED_PRODUCT_DESCRIPTION)
                .productIds(List.of("c", "d"))
                .build();

        // Act
        final var response = PUT_productPackage("901901901", request, ErrorResponse.class);
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(new ErrorResponse("package not found"), response.getBody());
    }

    @Test
    void updatePackage_returns400ifInvalidId() {
        // Arrange
        final var request = ChangePackageRequest.builder()
                .name(UPDATED_PRODUCT_NAME)
                .description(UPDATED_PRODUCT_DESCRIPTION)
                .productIds(List.of("c", "d"))
                .build();

        // Act
        final var response = PUT_productPackage("notanumber", request, ErrorResponse.class);
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(new ErrorResponse("invalid id: must be a number"), response.getBody());
    }

    @Test
    void updatePackage_persistsChanges() {
        // Arrange
        final var createRequest = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of("a", "b"))
                .build();

        ResponseEntity<PackageResource> creationResponse = POST_productPackage(createRequest);
        PackageResource createdEntity = creationResponse.getBody();

        final var request = ChangePackageRequest.builder()
                .name(UPDATED_PRODUCT_NAME)
                .description(UPDATED_PRODUCT_DESCRIPTION)
                .productIds(List.of("c", "d"))
                .build();

        // Act
        final var response = PUT_productPackage(createdEntity.id(), request);
        final var updatedEntity = GET_productPackage(createdEntity.id()).getBody();


        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(UPDATED_PRODUCT_NAME, updatedEntity.name());
        assertEquals(UPDATED_PRODUCT_DESCRIPTION, updatedEntity.description());
        assertEquals(List.of("c", "d"), updatedEntity.productIds());

    }

    // TODO: clean this up when ID is a string

    private ResponseEntity<PackageResource> GET_productPackage(final String id){
        return GET_productPackage(id, PackageResource.class);
    }

    private ResponseEntity<PackageResource> GET_productPackage(final long id){
        return GET_productPackage(Long.toString(id));
    }

    private <T> ResponseEntity<T> GET_productPackage(final long id, Class<T> entityClass){
        return GET_productPackage(Long.toString(id), entityClass);
    }

    private <T> ResponseEntity<T> GET_productPackage(final String id, Class<T> entityClass){
        return restTemplate.getForEntity("/packages/{id}", entityClass, id);
    }


    private ResponseEntity<PackageResource> PUT_productPackage(final long id, final ChangePackageRequest request){
        return PUT_productPackage(Long.toString(id), request);
    }

    private ResponseEntity<PackageResource> PUT_productPackage(final String id, final ChangePackageRequest request){
        return PUT_productPackage(id, request, PackageResource.class);
    }

    private <T> ResponseEntity<T> PUT_productPackage(final String id, final ChangePackageRequest request, Class<T> responseClass){
        final HttpEntity<ChangePackageRequest> httpRequest = new HttpEntity<>(request);
        return restTemplate.exchange(String.format("/packages/%s", id), HttpMethod.PUT, httpRequest, responseClass);
    }

    private ResponseEntity<PackageResource> POST_productPackage(final ChangePackageRequest request){
        return restTemplate.postForEntity("/packages", request, PackageResource.class);
    }

    private PackageResource provisionProductPackage(final String name, final String description) {
        final var newPackage = Package.builder()
                .name(name)
                .description(description)
                .build();
        Package createdPackage = packageRepository.save(newPackage);
        return PackageResource.fromModel(createdPackage, List.of());
    }

}
