package com.example.codingexercise.repository;

import com.example.codingexercise.model.PackageProduct;
import com.example.codingexercise.model.PackageProductId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageProductRepository extends JpaRepository<PackageProduct, PackageProductId> {

    List<PackageProduct> findAllById_PackageId(long packageId);
}
