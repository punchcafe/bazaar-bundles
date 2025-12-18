package com.example.codingexercise.repository;

import com.example.codingexercise.model.Package;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageRepository extends JpaRepository<Package, Long> {
    @Override
    Page<Package> findAll(@Nonnull Pageable request);
}
