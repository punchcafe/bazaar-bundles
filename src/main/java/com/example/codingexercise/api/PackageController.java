package com.example.codingexercise.api;

import com.example.codingexercise.api.schema.CreatePackageRequest;
import com.example.codingexercise.api.schema.PackageResource;
import com.example.codingexercise.model.Package;
import com.example.codingexercise.repository.PackageRepository;
import com.example.codingexercise.repository.ProductPackageRepository;
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
    public PackageResource create(@RequestBody CreatePackageRequest request) {
        final var newEntity = Package.builder()
                .name(request.name())
                .description(request.description())
                .build();

        final var createdPackage = packageRepository.save(newEntity);
        return PackageResource.fromModel(createdPackage);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/packages/{id}")
    public PackageResource get(@PathVariable String id) {
        // Test cases:
        // - invalid number: 400
        // - not found 404
        final var intId = Long.parseLong(id);
        return packageRepository.findById(intId).map(PackageResource::fromModel).orElseThrow();
    }
}
