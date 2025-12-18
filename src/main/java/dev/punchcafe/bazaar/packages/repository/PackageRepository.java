package dev.punchcafe.bazaar.packages.repository;

import dev.punchcafe.bazaar.packages.model.PackageOrm;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageRepository extends JpaRepository<PackageOrm, Long> {
    @Override
    Page<PackageOrm> findAll(@Nonnull Pageable request);
}
