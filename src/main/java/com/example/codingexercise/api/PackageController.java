package com.example.codingexercise.api;

import com.example.codingexercise.api.errors.EntityNotFoundException;
import com.example.codingexercise.api.schema.CreatePackageRequest;
import com.example.codingexercise.api.schema.ErrorResponse;
import com.example.codingexercise.api.schema.PackageResource;
import com.example.codingexercise.model.Package;
import com.example.codingexercise.model.PackageProduct;
import com.example.codingexercise.model.PackageProductId;
import com.example.codingexercise.repository.PackageProductRepository;
import com.example.codingexercise.repository.PackageRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
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
    public PackageResource create(@RequestBody CreatePackageRequest request) {
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


    private static PackageProduct buildPackageProduct(final String productId, final long packageId) {
        return PackageProduct.builder()
                .id(
                        PackageProductId.builder()
                                .packageId(packageId)
                                .productId(productId)
                                .build())
                .build();
    }


    @RequestMapping(method = RequestMethod.GET, value = "/packages/{id}")
    public PackageResource get(@PathVariable String id) {
        final var pkg = Optional.of(id)
                .map(Long::parseLong)
                .flatMap(packageRepository::findById)
                .orElseThrow(EntityNotFoundException::new);

        // TODO: replace with JPA annotations

        final var products = packageProductRepository.findAllById_PackageId(pkg.getId())
                .stream()
                .map(PackageProduct::getId)
                .map(PackageProductId::getProductId)
                .toList();

        return PackageResource.fromModel(pkg, products);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseBody
    ErrorResponse handleNotFound(final HttpServletRequest req, final Exception ex){
        return new ErrorResponse("package not found");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NumberFormatException.class)
    @ResponseBody
    ErrorResponse handleInvalidId(final HttpServletRequest req, final Exception ex){
        // Consider removing this before production and keeping it all strings at the
        // API level (so this would become 404).
        return new ErrorResponse("invalid id: must be a number");
    }
}
