package com.example.codingexercise.packages;

import com.example.codingexercise.api.schema.PackageResource;
import com.example.codingexercise.model.PackageOrm;
import com.example.codingexercise.model.PackageProduct;
import com.example.codingexercise.model.PackageProductId;
import com.example.codingexercise.repository.PackageProductRepository;
import com.example.codingexercise.repository.PackageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Component
public class PackageService {

    private final PackageRepository packageRepository;
    private final PackageProductRepository packageProductRepository;

    public PackageService(
            final PackageRepository packageRepository,
            final PackageProductRepository packageProductRepository
    ) {
        this.packageRepository = packageRepository;
        this.packageProductRepository = packageProductRepository;
    }

    public Optional<Package> get(final long id) {
        final var existingPackage = packageRepository.findById(id);
        if(existingPackage.isEmpty()) {
            return Optional.empty();
        }
        final var packageProducts = lookupProducts(id);
        return Optional.of(ormToModel(existingPackage.get(), packageProducts));
    }

    public Package create(final String name, final String description, final List<String> productIds) {
        final var newEntity = PackageOrm.builder()
                .name(name)
                .description(description)
                .build();

        final var createdPackage = packageRepository.save(newEntity);
        final var products = productIds.stream().map(
                id -> buildPackageProduct(id, createdPackage.getId())
        ).toList();

        // TODO: make transactional and try and fix in JPA mapping
        final List<PackageProduct> savedProducts = this.packageProductRepository.saveAll(products);

        return ormToModel(createdPackage, savedProducts);
    }

    public Package update(final long id, final String name, final String description, final List<String> productIds) {
        final var existingPackage = packageRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        final var existingProducts = lookupProducts(existingPackage.getId());

        // TODO: replace with JPA annotations and better joins
        // TODO: extract to serivce

        final var existingProductIds = productIds(existingProducts);

        final var updatedEntity = existingPackage.toBuilder()
                .name(name)
                .description(description)
                .build();

        final var deletedProducts = existingProducts.stream()
                .filter(packageProduct -> !productIds.contains(packageProduct.getId().getProductId()))
                .toList();

        final var addedProducts = productIds.stream()
                .filter(productId -> !existingProductIds.contains(productId))
                .map(productId -> buildPackageProduct(productId, existingPackage.getId()))
                .toList();

        // TODO: add to transaction

        final var persistedEntity = this.packageRepository.save(updatedEntity);
        this.packageProductRepository.deleteAll(deletedProducts);
        this.packageProductRepository.saveAll(addedProducts);

        final var updatedProducts = packageProductRepository.findAllById_PackageId(existingPackage.getId());

        return ormToModel(persistedEntity, updatedProducts);
    }

    public List<Package> pagenatedPackages(final int pageNumber, final int pageSize) {

        final var pageRequest = PageRequest.of(pageNumber,pageSize);

        final var allPackages = packageRepository.findAll(pageRequest);
        final var allPackageIds = allPackages.stream().map(PackageOrm::getId).toList();
        final var allPackageProducts = packageProductRepository.findAllById_PackageIdIn(allPackageIds)
                .stream()
                .collect(Collectors.groupingBy(packageProduct -> packageProduct.getId().getPackageId()));

        return allPackages
                .stream()
                .map(packageOrmEnity ->
                        ormToModel(
                                packageOrmEnity,
                                allPackageProducts.getOrDefault(packageOrmEnity.getId(), List.of()))
                )
                .toList();
    }

    public void delete(final long id) {
        final var existingPackage = packageRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        final var packageProducts = lookupProducts(existingPackage.getId());
        this.packageProductRepository.deleteAll(packageProducts);
        this.packageRepository.delete(existingPackage);
    }

    private List<PackageProduct> lookupProducts(final long id){
        return packageProductRepository.findAllById_PackageId(id)
                .stream()
                .toList();
    }

    private List<String> productIds(final List<PackageProduct> packageProducts){
        return packageProducts.stream()
                .map(PackageProduct::getId)
                .map(PackageProductId::getProductId)
                .toList();
    }


    private Package ormToModel(final PackageOrm model, final List<PackageProduct> packageProducts) {
        return Package.builder()
                .id(model.getId())
                .name(model.getName())
                .productIds(productIds(packageProducts))
                .description(model.getDescription())
                .build();
    }

    private PackageProduct buildPackageProduct(final String productId, final long packageId) {
        return PackageProduct.builder()
                .id(
                        PackageProductId.builder()
                                .packageId(packageId)
                                .productId(productId)
                                .build())
                .build();
    }
}
