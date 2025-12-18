package com.example.codingexercise;

import com.example.codingexercise.api.schema.ChangePackageRequest;
import com.example.codingexercise.api.schema.ErrorResponse;
import com.example.codingexercise.api.schema.ListPackageResponse;
import com.example.codingexercise.api.schema.PackageResource;
import com.example.codingexercise.config.ApiConfiguration;
import com.example.codingexercise.model.Package;
import com.example.codingexercise.repository.PackageProductRepository;
import com.example.codingexercise.repository.PackageRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.function.Function;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PackageControllerTests {

    private final static String TEST_PRODUCT_NAME = "Test Name";
    private final static String TEST_PRODUCT_DESCRIPTION = "Test Description";

    private final static String UPDATED_PRODUCT_NAME = "Updated Test Name";
    private final static String UPDATED_PRODUCT_DESCRIPTION = "Updated Test Description";

	private final TestRestTemplate restTemplate;
    private final PackageRepository packageRepository;
    private final PackageProductRepository packagePackageRepository;
    private final ApiConfiguration apiConfiguration;

    @Autowired
    PackageControllerTests(final TestRestTemplate restTemplate,
                           final PackageRepository packageRepository,
                           final PackageProductRepository packagePackageRepository,
                           final ApiConfiguration apiConfiguration) {
		this.restTemplate = restTemplate;
        this.packageRepository = packageRepository;
        this.packagePackageRepository = packagePackageRepository;
        this.apiConfiguration = apiConfiguration;
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


    @Test
    void deletePackage_returns200AndDeletesPackageIfItExists() {
        // Arrange
        final var createRequest = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of("a", "b"))
                .build();

        ResponseEntity<PackageResource> creationResponse = POST_productPackage(createRequest);
        PackageResource createdEntity = creationResponse.getBody();

        // Act
        final var deleteResponse = DELETE_productPackage(createdEntity.id());
        final var getResponse = GET_productPackage(createdEntity.id());


        // Assert
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());
        assertNull(deleteResponse.getBody());
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }


    // TODO:
    // Ensure nothing else is deleted, (including products)

    @Test
    void deletePackage_returns400IfInvalidID() {
        // Act
        final var deleteResponse = DELETE_productPackage("invalid_id", ErrorResponse.class);


        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, deleteResponse.getStatusCode());
        assertEquals(new ErrorResponse("invalid id: must be a number"), deleteResponse.getBody());
    }

    @Test
    void deletePackage_returns404IfNotFound() {
        // Act
        final var deleteResponse = DELETE_productPackage("101202303404", ErrorResponse.class);


        // Assert
        assertEquals(HttpStatus.NOT_FOUND, deleteResponse.getStatusCode());
        assertEquals(new ErrorResponse("package not found"), deleteResponse.getBody());
    }

    record ListTestCase(int pageSize, int pageNumber, List<Integer> expectedEntitySeeds){};

    @Test
    void listPackages_correctlyPaginatesByParameters() {
        // Default ordering is by insertion order, so we know this creates 10 ordered
        // packages using seed 0..9

        generateEmptyPackages(10);

        final var cases = List.of(
                new ListTestCase(1, 0, List.of(0)),
                new ListTestCase(2, 0, List.of(0, 1)),
                new ListTestCase(3, 0, List.of(0, 1, 2)),
                new ListTestCase(3, 1, List.of(3, 4, 5)),
                new ListTestCase(3, 2, List.of(6, 7, 8)),
                new ListTestCase(3, 3, List.of(9)),
                // Out of range
                new ListTestCase(3, 4, List.of())
        );

        for(final var paginationCase : cases) {
            final var response = GET_productPackages(paginationCase.pageSize, paginationCase.pageNumber);
            final var expectedNames = paginationCase.expectedEntitySeeds.stream()
                    .map(this::generateName)
                    .toList();
            assertEquals(HttpStatus.OK, response.getStatusCode());
            final var actualNames = response.getBody().packages().stream().map(PackageResource::name).toList();

            assertEquals(paginationCase.pageNumber, response.getBody().pageNumber());
            assertEquals(paginationCase.pageSize, response.getBody().pageSize());
            assertEquals(expectedNames, actualNames);
        }
    }

    @Test
    void listPackages_returns400OnInvalidQueryParams() {

        // Act

        final var negativeSizeResponse = GET_productPackages(-1, 1, ErrorResponse.class);
        final var negativeNumberResponse = GET_productPackages(1, -1, ErrorResponse.class);
        final var zeroPageSizeResponse = GET_productPackages(0, -1, ErrorResponse.class);
        final var invalidPageSizeResponse =  restTemplate.getForEntity(
                "/packages?page_size={pageSize}&page_number={pageNumber}",
                ErrorResponse.class,
                "hello",
                1);
        final var invalidPageNumberResponse =  restTemplate.getForEntity(
                "/packages?page_size={pageSize}&page_number={pageNumber}",
                ErrorResponse.class,
                1,
                "hello");

        // Assert

        assertEquals(HttpStatus.BAD_REQUEST, negativeSizeResponse.getStatusCode());
        assertEquals(new ErrorResponse("invalid query pagination parameters"), negativeSizeResponse.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, negativeNumberResponse.getStatusCode());
        assertEquals(new ErrorResponse("invalid query pagination parameters"), negativeNumberResponse.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, zeroPageSizeResponse.getStatusCode());
        assertEquals(new ErrorResponse("invalid query pagination parameters"), zeroPageSizeResponse.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, invalidPageSizeResponse.getStatusCode());
        assertEquals(new ErrorResponse("invalid query pagination parameters"), invalidPageSizeResponse.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, invalidPageNumberResponse.getStatusCode());
        assertEquals(new ErrorResponse("invalid query pagination parameters"), invalidPageNumberResponse.getBody());
    }

    void listPackages_scalesPageSizeDownIfBiggerThanMax() {
        final var result = GET_productPackages(this.apiConfiguration.getMaxPageSize() + 1, 1);
        assertEquals(result.getBody().pageSize(), this.apiConfiguration.getMaxPageSize());
    }

    @Test
    void listPackages_containsFullProductEntity() {
        // Arrange

        final var firstRequest = ChangePackageRequest.builder()
                .name("Sample")
                .description("Description")
                .productIds(List.of("a", "b", "c"))
                .build();

        final var secondRequest = ChangePackageRequest.builder()
                .name("OtherSample")
                .description("Another description")
                .productIds(List.of("d", "e", "f"))
                .build();

        final var firstCreated = POST_productPackage(firstRequest);
        final var secondCreated = POST_productPackage(secondRequest);

        Assumptions.assumeTrue(HttpStatus.CREATED.equals(firstCreated.getStatusCode()));
        Assumptions.assumeTrue(HttpStatus.CREATED.equals(secondCreated.getStatusCode()));

        final var expectedEntries = List.of(
                firstCreated.getBody(),
                secondCreated.getBody()
        );

        // Act

        final var response = GET_productPackages(2, 0);

        // Assert

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // May need to update this in case productID ordering (not-guaranteed) creates equality issues.
        assertEquals(expectedEntries, response.getBody().packages());
    }

    private void generateEmptyPackages(final int count) {
        for(int i = 0; i < count; i++){
            final var request = ChangePackageRequest.builder()
                    .name(generateName(i))
                    .productIds(List.of())
                    .build();
            final var created = POST_productPackage(request);
            Assumptions.assumeTrue(HttpStatus.CREATED.equals(created.getStatusCode()));
        }
    }

    private String generateName(final int index) {
        return String.format("list_test_item_%d", index);
    }

    @BeforeEach
    public void clearDatabase() {
        this.packagePackageRepository.deleteAll();
        this.packageRepository.deleteAll();;
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


    private ResponseEntity<ListPackageResponse> GET_productPackages(final int pageSize, final int pageNumber){
        return GET_productPackages(pageSize, pageNumber, ListPackageResponse.class);
    }

    private <T> ResponseEntity<T> GET_productPackages(final int pageSize, final int pageNumber, Class<T> expectedResponse){
        return restTemplate.getForEntity(
                "/packages?page_size={pageSize}&page_number={pageNumber}",
                expectedResponse,
                pageSize,
                pageNumber);
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

    private ResponseEntity<Void> DELETE_productPackage(final long id){
        return DELETE_productPackage(Long.toString(id), Void.class);
    }

    private <T> ResponseEntity<T> DELETE_productPackage(final String id,  Class<T> responseClass){
        return restTemplate.exchange(String.format("/packages/%s", id), HttpMethod.DELETE, HttpEntity.EMPTY, responseClass);
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
