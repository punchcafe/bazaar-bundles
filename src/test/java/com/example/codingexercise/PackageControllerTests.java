package com.example.codingexercise;

import com.example.codingexercise.api.schema.ChangePackageRequest;
import com.example.codingexercise.api.schema.ErrorResponse;
import com.example.codingexercise.api.schema.ListPackageResponse;
import com.example.codingexercise.api.schema.PackageResource;
import com.example.codingexercise.config.ApiConfiguration;
import com.example.codingexercise.packages.Package;
import com.example.codingexercise.packages.PackageService;
import com.example.codingexercise.packages.repository.PackageProductRepository;
import com.example.codingexercise.packages.repository.PackageRepository;
import com.example.codingexercise.products.Product;
import com.example.codingexercise.products.ProductsCache;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PackageControllerTests {

    private final static String TEST_PRODUCT_NAME = "Test Name";
    private final static String TEST_PRODUCT_DESCRIPTION = "Test Description";

    private final static String UPDATED_PRODUCT_NAME = "Updated Test Name";
    private final static String UPDATED_PRODUCT_DESCRIPTION = "Updated Test Description";

    private final static String SAMPLE_PRODUCT_ID_1 = "VqKb4tyj9V6i";
    private final static String SAMPLE_PRODUCT_ID_2 = "DXSQpv6XVeJm";
    private final static String SAMPLE_PRODUCT_ID_3 = "7dgX6XzU3Wds";
    private final static String SAMPLE_PRODUCT_ID_4 = "PKM5pGAh9yGm";

    private final static Product SAMPLE_PRODUCT_1 = new Product(SAMPLE_PRODUCT_ID_1, "Shield", 1149);
    private final static Product SAMPLE_PRODUCT_2 = new Product(SAMPLE_PRODUCT_ID_2, "Helmet", 999);
    private final static Product SAMPLE_PRODUCT_3 = new Product(SAMPLE_PRODUCT_ID_3, "Sword", 899);
    private final static Product SAMPLE_PRODUCT_4 = new Product(SAMPLE_PRODUCT_ID_4, "Axe", 799);

    private final static Map<String, Product> SAMPLE_PRODUCT_CACHE = Map.of(
            SAMPLE_PRODUCT_ID_1, SAMPLE_PRODUCT_1,
            SAMPLE_PRODUCT_ID_2, SAMPLE_PRODUCT_2,
            SAMPLE_PRODUCT_ID_3, SAMPLE_PRODUCT_3,
            SAMPLE_PRODUCT_ID_4, SAMPLE_PRODUCT_4
    );

    @MockBean private final ProductsCache productsCache;
    private final TestRestTemplate restTemplate;
    private final PackageRepository packageRepository;
    private final PackageProductRepository packagePackageRepository;
    private final ApiConfiguration apiConfiguration;
    private final PackageService packageService;

    @Autowired
    PackageControllerTests(
            final ProductsCache productsCache,
            final TestRestTemplate restTemplate,
            final PackageRepository packageRepository,
            final PackageProductRepository packagePackageRepository,
            final PackageService packageService,
            final ApiConfiguration apiConfiguration) {
        this.productsCache = productsCache;
        this.restTemplate = restTemplate;
        this.packageRepository = packageRepository;
        this.packagePackageRepository = packagePackageRepository;
        this.packageService = packageService;
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
    void createPackage_returns400IfProductIDNotRegistered() {
        // Arrange
        final var request = ChangePackageRequest.builder()
                .name(UPDATED_PRODUCT_NAME)
                .description(UPDATED_PRODUCT_DESCRIPTION)
                .productIds(List.of("unknown_id", SAMPLE_PRODUCT_ID_1))
                .build();

        // Act
        final var response = POST_productPackage(request, ErrorResponse.class);
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(new ErrorResponse("unknown product ID"), response.getBody());
    }

    @Test
    void createPackage_returnsPersistedProductIdsInResponse() {
        // Arrange
        final var request = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of(SAMPLE_PRODUCT_ID_1, SAMPLE_PRODUCT_ID_2, SAMPLE_PRODUCT_ID_3))
                .build();

        // Act
        ResponseEntity<PackageResource> response = POST_productPackage(request);
        PackageResource responseBody = response.getBody();

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertThat(List.of(SAMPLE_PRODUCT_ID_1, SAMPLE_PRODUCT_ID_2, SAMPLE_PRODUCT_ID_3))
                .containsExactlyInAnyOrderElementsOf(responseBody.productIds());
    }

    @Test
    void createPackage_persistsProductIds() {
        // Arrange
        final var request = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of(SAMPLE_PRODUCT_ID_1, SAMPLE_PRODUCT_ID_2))
                .build();

        // Act
        ResponseEntity<PackageResource> response = POST_productPackage(request);
        PackageResource responseBody = response.getBody();

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertThat(List.of(SAMPLE_PRODUCT_ID_1, SAMPLE_PRODUCT_ID_2))
                .containsExactlyInAnyOrderElementsOf(responseBody.productIds());
    }
    // TODO: validate against duplicate productIds

    @Test
    void createPackageWithProductIds_andGetPackage_returnsCreatedProductIds() {
        // Arrange
        final var request = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of(SAMPLE_PRODUCT_ID_4, SAMPLE_PRODUCT_ID_1))
                .build();

        // Act
        ResponseEntity<PackageResource> response = POST_productPackage(request);
        PackageResource createdBody = response.getBody();
        final var createdId = createdBody.id();
        final var getProductPackageResponse = GET_productPackage(createdId);

        // Assert
        assertEquals(HttpStatus.OK, getProductPackageResponse.getStatusCode());
        assertThat(List.of(SAMPLE_PRODUCT_ID_4, SAMPLE_PRODUCT_ID_1))
                .containsExactlyInAnyOrderElementsOf(getProductPackageResponse.getBody().productIds());
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
    void createPackage_andGetPackage_includesCalculatedCost() {
        // Arrange
        final var request = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of(SAMPLE_PRODUCT_ID_1, SAMPLE_PRODUCT_ID_2))
                .build();

        final var expectedPrice = SAMPLE_PRODUCT_1.usdPrice() + SAMPLE_PRODUCT_2.usdPrice();
        // Act
        ResponseEntity<PackageResource> response = POST_productPackage(request);
        PackageResource createdBody = response.getBody();
        final var createdId = createdBody.id();
        final var getProductPackageResponse = GET_productPackage(createdId);

        // Assert
        assertEquals(HttpStatus.OK, getProductPackageResponse.getStatusCode());
        assertEquals(expectedPrice, createdBody.totalPrice());
        assertEquals(expectedPrice, getProductPackageResponse.getBody().totalPrice());
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
                .productIds(List.of(SAMPLE_PRODUCT_ID_4, SAMPLE_PRODUCT_ID_1))
                .build();

        ResponseEntity<PackageResource> creationResponse = POST_productPackage(createRequest);
        PackageResource createdEntity = creationResponse.getBody();

        final var request = ChangePackageRequest.builder()
                .name(UPDATED_PRODUCT_NAME)
                .description(UPDATED_PRODUCT_DESCRIPTION)
                .productIds(List.of(SAMPLE_PRODUCT_ID_3, SAMPLE_PRODUCT_ID_2))
                .build();

        // Act
        final var response = PUT_productPackage(createdEntity.id(), request);
        final var updatedEntity = response.getBody();


        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(UPDATED_PRODUCT_NAME, updatedEntity.name());
        assertEquals(UPDATED_PRODUCT_DESCRIPTION, updatedEntity.description());
        assertThat(List.of(SAMPLE_PRODUCT_ID_3, SAMPLE_PRODUCT_ID_2))
                .containsExactlyInAnyOrderElementsOf(updatedEntity.productIds());
    }


    @Test
    void updatePackage_canSetProductsToEmpty() {
        // Arrange
        final var createRequest = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of(SAMPLE_PRODUCT_ID_3))
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
                .productIds(List.of(SAMPLE_PRODUCT_ID_1, SAMPLE_PRODUCT_ID_3))
                .build();

        ResponseEntity<PackageResource> creationResponse = POST_productPackage(createRequest);
        PackageResource createdEntity = creationResponse.getBody();

        final var request = ChangePackageRequest.builder()
                .productIds(List.of(SAMPLE_PRODUCT_ID_1, SAMPLE_PRODUCT_ID_2))
                .build();

        // Act
        final var response = PUT_productPackage(createdEntity.id(), request);
        final var updatedEntity = response.getBody();


        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertThat(List.of(SAMPLE_PRODUCT_ID_1, SAMPLE_PRODUCT_ID_2))
                .containsExactlyInAnyOrderElementsOf(updatedEntity.productIds());
    }

    @Test
    void updatePackage_updatesPriceIfProductsChange() {
        // Arrange
        final var createRequest = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of(SAMPLE_PRODUCT_ID_3))
                .build();

        ResponseEntity<PackageResource> creationResponse = POST_productPackage(createRequest);
        PackageResource createdEntity = creationResponse.getBody();

        Assumptions.assumeTrue(createdEntity.totalPrice() == SAMPLE_PRODUCT_3.usdPrice());

        final var request = ChangePackageRequest.builder()
                .productIds(List.of(SAMPLE_PRODUCT_ID_3, SAMPLE_PRODUCT_ID_4))
                .build();

        // Act
        final var response = PUT_productPackage(createdEntity.id(), request);
        final var updatedEntity = response.getBody();


        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(SAMPLE_PRODUCT_3.usdPrice() + SAMPLE_PRODUCT_4.usdPrice(), updatedEntity.totalPrice());
    }

    @Test
    void updatePackage_returns404ifPackageDoesntExist() {
        // Arrange
        final var request = ChangePackageRequest.builder()
                .name(UPDATED_PRODUCT_NAME)
                .description(UPDATED_PRODUCT_DESCRIPTION)
                .productIds(List.of(SAMPLE_PRODUCT_ID_2))
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
                .productIds(List.of(SAMPLE_PRODUCT_ID_1))
                .build();

        // Act
        final var response = PUT_productPackage("notanumber", request, ErrorResponse.class);
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(new ErrorResponse("invalid id: must be a number"), response.getBody());
    }

    @Test
    void updatePackage_returns400IfProductIDNotRegistered() {
        // Arrange
        final var createRequest = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of(SAMPLE_PRODUCT_ID_3))
                .build();

        ResponseEntity<PackageResource> creationResponse = POST_productPackage(createRequest);
        PackageResource createdEntity = creationResponse.getBody();

        final var request = ChangePackageRequest.builder()
                .name(UPDATED_PRODUCT_NAME)
                .description(UPDATED_PRODUCT_DESCRIPTION)
                .productIds(List.of("unknown_id", SAMPLE_PRODUCT_ID_1))
                .build();

        // Act
        final var response = PUT_productPackage(createdEntity.id(), request, ErrorResponse.class);
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(new ErrorResponse("unknown product ID"), response.getBody());
    }

    @Test
    void updatePackage_persistsChanges() {
        // Arrange
        final var createRequest = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of(SAMPLE_PRODUCT_ID_4))
                .build();

        ResponseEntity<PackageResource> creationResponse = POST_productPackage(createRequest);
        PackageResource createdEntity = creationResponse.getBody();

        final var request = ChangePackageRequest.builder()
                .name(UPDATED_PRODUCT_NAME)
                .description(UPDATED_PRODUCT_DESCRIPTION)
                .productIds(List.of(SAMPLE_PRODUCT_ID_1, SAMPLE_PRODUCT_ID_2))
                .build();

        // Act
        final var response = PUT_productPackage(createdEntity.id(), request);
        final var updatedEntity = GET_productPackage(createdEntity.id()).getBody();


        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(UPDATED_PRODUCT_NAME, updatedEntity.name());
        assertEquals(UPDATED_PRODUCT_DESCRIPTION, updatedEntity.description());
        assertThat(List.of(SAMPLE_PRODUCT_ID_1, SAMPLE_PRODUCT_ID_2))
                .containsAnyElementsOf(updatedEntity.productIds());
    }


    @Test
    void deletePackage_returns200AndDeletesPackageIfItExists() {
        // Arrange
        final var createRequest = ChangePackageRequest.builder()
                .name(TEST_PRODUCT_NAME)
                .description(TEST_PRODUCT_DESCRIPTION)
                .productIds(List.of(SAMPLE_PRODUCT_ID_1))
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
                .productIds(List.of(SAMPLE_PRODUCT_ID_2, SAMPLE_PRODUCT_ID_3))
                .build();

        final var secondRequest = ChangePackageRequest.builder()
                .name("OtherSample")
                .description("Another description")
                .productIds(List.of(SAMPLE_PRODUCT_ID_1, SAMPLE_PRODUCT_ID_4))
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
        for(int i = 0; i < expectedEntries.size(); i++){
            final var expected = expectedEntries.get(0);
            final var actual = response.getBody().packages().get(0);
            assertEquals(expected.totalPrice(), actual.totalPrice());
            assertEquals(expected.id(), actual.id());
            assertEquals(expected.name(), actual.name());
            assertEquals(expected.description(), actual.description());
            assertThat(expected.productIds()).containsExactlyInAnyOrderElementsOf(actual.productIds());
        }
        // May need to update this in case productID ordering (not-guaranteed) creates equality issues.
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
        when(productsCache.getCache()).thenReturn(SAMPLE_PRODUCT_CACHE);
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

    private <T> ResponseEntity<T> PUT_productPackage(final long id, final ChangePackageRequest request, Class<T> responseClass){
        return PUT_productPackage(Long.toString(id), request, responseClass);
    }

    private ResponseEntity<PackageResource> POST_productPackage(final ChangePackageRequest request){
        return POST_productPackage(request, PackageResource.class);
    }

    private <T> ResponseEntity<T> POST_productPackage(final ChangePackageRequest request, Class<T> responseClass){
        return restTemplate.postForEntity("/packages", request, responseClass);
    }

    private ResponseEntity<Void> DELETE_productPackage(final long id){
        return DELETE_productPackage(Long.toString(id), Void.class);
    }

    private <T> ResponseEntity<T> DELETE_productPackage(final String id,  Class<T> responseClass){
        return restTemplate.exchange(String.format("/packages/%s", id), HttpMethod.DELETE, HttpEntity.EMPTY, responseClass);
    }

    private Package provisionProductPackage(final String name, final String description) {
        return packageService.create(name, description, List.of());
    }

}
