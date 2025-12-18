package com.example.codingexercise.packages.repository;

import com.example.codingexercise.packages.model.PackageProduct;
import com.example.codingexercise.packages.model.PackageProductId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageProductRepository extends JpaRepository<PackageProduct, PackageProductId> {

    List<PackageProduct> findAllById_PackageId(long packageId);
    List<PackageProduct> findAllById_PackageIdIn(List<Long> packageIds);
}
