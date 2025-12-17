package com.example.codingexercise.repository;

import com.example.codingexercise.model.Package;
import org.springframework.data.repository.CrudRepository;

public interface PackageRepository extends CrudRepository<Package, Long> {
}
