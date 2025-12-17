package com.example.codingexercise.repository;

import com.example.codingexercise.model.Package;
import com.example.codingexercise.model.PackageProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PackageProductRepository extends JpaRepository<PackageProduct, Long> {

    List<PackageProduct> findAllById_PackageId(long packageId);
}
