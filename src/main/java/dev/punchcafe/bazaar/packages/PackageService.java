package dev.punchcafe.bazaar.packages;

import dev.punchcafe.bazaar.packages.model.PackageOrm;
import dev.punchcafe.bazaar.packages.model.PackageProduct;
import dev.punchcafe.bazaar.packages.model.PackageProductId;
import dev.punchcafe.bazaar.packages.repository.PackageProductRepository;
import dev.punchcafe.bazaar.packages.repository.PackageRepository;
import lombok.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The PackageService encapsulates all persistent operations involving Packages.
 */
@Component
public class PackageService {
    // TODO: add unit tests.

    private final PackageRepository packageRepository;
    private final PackageProductRepository packageProductRepository;

    public PackageService(
            final PackageRepository packageRepository,
            final PackageProductRepository packageProductRepository
    ) {
        this.packageRepository = packageRepository;
        this.packageProductRepository = packageProductRepository;
    }

    /**
     * Retrieves a package by its id.
     * @param id the id of the package
     * @return an optional containing the package. Empty if no package found with that ID.
     */
    public Optional<Package> get(final long id) {
        final var existingPackage = packageRepository.findById(id);
        if(existingPackage.isEmpty()) {
            return Optional.empty();
        }
        final var packageProducts = lookupProducts(id);
        return Optional.of(ormToModel(existingPackage.get(), packageProducts));
    }

    /**
     * Create a Package with the given parameters.
     *
     * @param name the human-readable name for the package.
     * @param description the human-readable description of the package
     * @param productIds the ids of all products in this package.
     * @return the created package.
     */
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

    /**
     * Updates a given package with the params.
     * Note that this updates _all_ passed values, so anything null or empty will overrwite the old value.
     * If the referenced package doesn't exist, this will through an EntityNotFound exception.
     *
     * @param id the id of the package to update
     * @param name the new human-readable name for the package
     * @param description the new human-readable description for the package
     * @param productIds ids of all products in this package.
     * @return the updated package.
     */
    public Package update(final long id, final String name, final String description, @NonNull final List<String> productIds) {
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

    /**
     * Retrieves all packages for a given page size and number.
     *
     * @param pageNumber the size of the page
     * @param pageSize the index (0 being the first) of the page.
     * @return the list of packages on that page.
     */
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

    /**
     * Deletes the given package.
     * Throws an EntityNotFoundException if the package doesn't exist.
     *
     * @param id the id of the package to delete.
     */
    public void delete(final long id) {
        // TODO: make transactional
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
