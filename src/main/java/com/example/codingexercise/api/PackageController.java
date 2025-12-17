package com.example.codingexercise.api;

import com.example.codingexercise.api.errors.EntityNotFoundException;
import com.example.codingexercise.api.schema.CreatePackageRequest;
import com.example.codingexercise.api.schema.ErrorResponse;
import com.example.codingexercise.api.schema.PackageResource;
import com.example.codingexercise.model.Package;
import com.example.codingexercise.repository.PackageRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.antlr.v4.runtime.atn.ErrorInfo;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class PackageController {

    private final PackageRepository packageRepository;

    public PackageController(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    @ResponseStatus(code=HttpStatus.CREATED)
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
        return Optional.of(id)
                .map(Long::parseLong)
                .flatMap(packageRepository::findById)
                .map(PackageResource::fromModel)
                .orElseThrow(EntityNotFoundException::new);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseBody
    ErrorResponse handleNotFound(final HttpServletRequest req, final Exception ex){
        return new ErrorResponse("package not found");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NumberFormatException.class)
    @ResponseBody
    ErrorResponse handleInvalidId(final HttpServletRequest req, final Exception ex){
        // Consider removing this before production and keeping it all strings at the
        // API level (so this would become 404).
        return new ErrorResponse("invalid id: must be a number");
    }
}
