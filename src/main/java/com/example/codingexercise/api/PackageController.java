package com.example.codingexercise.api;

import com.example.codingexercise.api.errors.EntityNotFoundException;
import com.example.codingexercise.api.schema.ChangePackageRequest;
import com.example.codingexercise.api.schema.ErrorResponse;
import com.example.codingexercise.api.schema.PackageResource;
import com.example.codingexercise.model.Package;
import com.example.codingexercise.model.PackageProduct;
import com.example.codingexercise.model.PackageProductId;
import com.example.codingexercise.repository.PackageProductRepository;
import com.example.codingexercise.repository.PackageRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class PackageController {

    private final PackageRepository packageRepository;
    private final PackageProductRepository packageProductRepository;

    public PackageController(
            final PackageRepository packageRepository,
            final PackageProductRepository packageProductRepository
    ) {
        this.packageRepository = packageRepository;
        this.packageProductRepository = packageProductRepository;
    }

    @ResponseStatus(code=HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, value = "/packages")
    public PackageResource create(@RequestBody ChangePackageRequest request) {
        final var newEntity = Package.builder()
                .name(request.name())
                .description(request.description())
                .build();

        final var createdPackage = packageRepository.save(newEntity);
        final var products = request.productIds().stream().map(
                id -> buildPackageProduct(id, createdPackage.getId())
        ).toList();

        // TODO: make transactional and try and fix in JPA mapping
        final List<String> savedProducts = new ArrayList<>();
        for(final var product : this.packageProductRepository.saveAll(products)){
            savedProducts.add(product.getId().getProductId());
        }

        return PackageResource.fromModel(createdPackage, savedProducts);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/packages/{id}")
    public PackageResource get(@PathVariable String id) {
        final var existingPackage = lookup(id);
        final var packageIds = lookupProducts(existingPackage.getId()).stream()
                .map(PackageProduct::getId)
                .map(PackageProductId::getProductId)
                .toList();
        return PackageResource.fromModel(lookup(id), packageIds);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/packages/{id}")
    public PackageResource update(@PathVariable String id, @RequestBody ChangePackageRequest request) {
        final var existingPackage = lookup(id);
        final var existingProducts = lookupProducts(existingPackage.getId());

        // TODO: replace with JPA annotations and better joins
        // TODO: extract to serivce

        final var existingProductIds = productIds(existingProducts);

        final var updatedEntity = existingPackage.toBuilder()
                .name(request.name())
                .description(request.description())
                .build();

        final var deleteProducts = existingProducts.stream()
                .filter(packageProduct -> !request.productIds().contains(packageProduct.getId().getProductId()))
                .toList();

        final var addProducts = request.productIds().stream()
                .filter(productId -> !existingProductIds.contains(productId))
                .map(productId -> buildPackageProduct(productId, existingPackage.getId()))
                .toList();

        final var persistedEntity = this.packageRepository.save(updatedEntity);
        this.packageProductRepository.deleteAll(deleteProducts);
        this.packageProductRepository.saveAll(addProducts);

        final var updatedProducts = packageProductRepository.findAllById_PackageId(existingPackage.getId())
                .stream()
                .map(PackageProduct::getId)
                .map(PackageProductId::getProductId)
                .toList();

        return PackageResource.fromModel(persistedEntity, updatedProducts);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(method = RequestMethod.DELETE, value = "/packages/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        // TODO:make transactional.
        final var existingPackage = lookup(id);
        final var packageProducts = lookupProducts(existingPackage.getId());
        this.packageProductRepository.deleteAll(packageProducts);
        this.packageRepository.delete(existingPackage);
        return ResponseEntity.noContent().build();
    }

    private Package lookup(final String id){
        return Optional.of(id)
                .map(Long::parseLong)
                .flatMap(packageRepository::findById)
                .orElseThrow(EntityNotFoundException::new);
    }

    private List<PackageProduct> lookupProducts(final long id){
        return packageProductRepository.findAllById_PackageId(id)
                .stream()
                .toList();
    }

    private List<String> productIds(final List<PackageProduct> packageProducts) {
        return packageProducts.stream()
                .map(PackageProduct::getId)
                .map(PackageProductId::getProductId)
                .toList();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseBody
    private ErrorResponse handleNotFound(final HttpServletRequest req, final Exception ex){
        return new ErrorResponse("package not found");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NumberFormatException.class)
    @ResponseBody
    private ErrorResponse handleInvalidId(final HttpServletRequest req, final Exception ex){
        // Consider removing this before production and keeping it all strings at the
        // API level (so this would become 404).
        return new ErrorResponse("invalid id: must be a number");
    }

    private static PackageProduct buildPackageProduct(final String productId, final long packageId) {
        return PackageProduct.builder()
                .id(
                        PackageProductId.builder()
                                .packageId(packageId)
                                .productId(productId)
                                .build())
                .build();
    }
}
