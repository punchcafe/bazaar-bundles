package dev.punchcafe.bazaar.packages.repository;

import dev.punchcafe.bazaar.packages.model.PackageOrm;
import dev.punchcafe.bazaar.packages.model.PackageProduct;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AtomicOperator {

    private final PackageRepository packageRepository;
    private final PackageProductRepository packageProductRepository;

    public AtomicOperator(
            final PackageRepository packageRepository,
            final PackageProductRepository packageProductRepository){
        this.packageRepository = packageRepository;
        this.packageProductRepository = packageProductRepository;
    }

    @Transactional
    public PackageOrm updatePackageAndProducts(
            final PackageOrm updatedEntity,
            final List<PackageProduct> productsToAdd,
            final List<PackageProduct> productsToDelete) {
        final var persistedEntity = this.packageRepository.save(updatedEntity);
        this.packageProductRepository.deleteAll(productsToDelete);
        this.packageProductRepository.saveAll(productsToAdd);
        return persistedEntity;
    }

    @Transactional
    public void deletePackageAndProducts(final PackageOrm entityToDelete) {
        this.packageProductRepository.deleteAllById_PackageId(entityToDelete.getId());
        this.packageRepository.delete(entityToDelete);
        return;
    }
}
