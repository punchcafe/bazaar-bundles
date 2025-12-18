package com.example.codingexercise.api;

import com.example.codingexercise.packages.EntityNotFoundException;
import com.example.codingexercise.api.errors.InvalidPaginationParameters;
import com.example.codingexercise.api.schema.ChangePackageRequest;
import com.example.codingexercise.api.schema.ErrorResponse;
import com.example.codingexercise.api.schema.ListPackageResponse;
import com.example.codingexercise.api.schema.PackageResource;
import com.example.codingexercise.config.ApiConfiguration;
import com.example.codingexercise.packages.PackageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
public class PackageController {

    private final ApiConfiguration apiConfiguration;
    private final PackageService packageService;

    public PackageController(
            final ApiConfiguration apiConfiguration,
            final PackageService packageService
    ) {
        this.apiConfiguration = apiConfiguration;
        this.packageService = packageService;
    }

    @ResponseStatus(code=HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, value = "/packages")
    public PackageResource create(@RequestBody ChangePackageRequest request) {
        final var createdPackage = packageService.create(request.name(), request.description(), request.productIds());
        return PackageResource.fromModel(createdPackage);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/packages/{id}")
    public PackageResource get(@PathVariable String id) {
         return Optional.of(id)
                .map(Long::parseLong)
                .flatMap(packageService::get)
                 .map(PackageResource::fromModel)
                .orElseThrow(EntityNotFoundException::new);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/packages/{id}")
    public PackageResource update(@PathVariable String id, @RequestBody ChangePackageRequest request) {
        final var parsedId = Long.parseLong(id);
        final var updatedPackage = packageService.update(
                parsedId,
                request.name(),
                request.description(),
                request.productIds()
        );
        return PackageResource.fromModel(updatedPackage);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(method = RequestMethod.DELETE, value = "/packages/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        final var parsedId = Long.parseLong(id);
        this.packageService.delete(parsedId);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/packages")
    public ListPackageResponse list(
            @RequestParam(value = "page_size", defaultValue = "10") String pageSizeString,
            @RequestParam(value = "page_number", defaultValue = "0") String pageNumberString
    ) {
        final var pageSize = Integer.min(
                validatePaginationParamString(pageSizeString),
                this.apiConfiguration.getMaxPageSize()
        );
        final var pageNumber = validatePaginationParamString(pageNumberString);

        if((pageSize <= 0) || pageNumber < 0) {
            throw new InvalidPaginationParameters();
        }

        final var resultEntries = packageService.pagenatedPackages(pageNumber, pageSize).stream()
                .map(PackageResource::fromModel)
                .toList();

        return ListPackageResponse.builder()
                .packages(resultEntries)
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .build();
    }

    private int validatePaginationParamString(final String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw  new InvalidPaginationParameters();
        }
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseBody
    private ErrorResponse handleNotFound(final HttpServletRequest req, final Exception ex){
        return new ErrorResponse("package not found");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NumberFormatException.class)
    @ResponseBody
    private ErrorResponse handleInvalidId(final HttpServletRequest req, final Exception ex){
        // Consider removing this before production and keeping it all strings at the
        // API level (so this would become 404).
        return new ErrorResponse("invalid id: must be a number");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidPaginationParameters.class)
    @ResponseBody
    private ErrorResponse handleInvalidPaginationParams(final HttpServletRequest req, final Exception ex){
        return new ErrorResponse("invalid query pagination parameters");
    }


}
