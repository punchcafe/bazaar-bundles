package com.example.codingexercise.api;

import com.example.codingexercise.api.schema.CreateProductPackageRequest;
import com.example.codingexercise.api.schema.ProductPackageResource;
import com.example.codingexercise.repository.PackageRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PackageController {

    private final PackageRepository packageRepository;

    public PackageController(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/packages")
    public ProductPackageResource create(@RequestBody CreateProductPackageRequest request) {
        final var createdProductPackage = packageRepository.create(
                request.name(),
                request.description(),
                request.productIds()
        );
        return ProductPackageResource.fromModel(createdProductPackage);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/packages/{id}")
    public ProductPackageResource get(@PathVariable String id) {
        return ProductPackageResource.fromModel(packageRepository.get(id));
    }
}
