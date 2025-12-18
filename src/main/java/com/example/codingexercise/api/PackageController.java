package com.example.codingexercise.api;

import com.example.codingexercise.api.errors.UnknownProductId;
import com.example.codingexercise.packages.EntityNotFoundException;
import com.example.codingexercise.api.errors.InvalidPaginationParameters;
import com.example.codingexercise.api.schema.ChangePackageRequest;
import com.example.codingexercise.api.schema.ErrorResponse;
import com.example.codingexercise.api.schema.ListPackageResponse;
import com.example.codingexercise.api.schema.PackageResource;
import com.example.codingexercise.config.ApiConfiguration;
import com.example.codingexercise.packages.Package;
import com.example.codingexercise.packages.PackageService;
import com.example.codingexercise.products.Product;
import com.example.codingexercise.products.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@Slf4j
@RestController
public class PackageController {

    private final ApiConfiguration apiConfiguration;
    private final PackageService packageService;
    private final ProductService productService;

    public PackageController(
            final ApiConfiguration apiConfiguration,
            final PackageService packageService,
            final ProductService productService
            ) {
        this.apiConfiguration = apiConfiguration;
        this.packageService = packageService;
        this.productService = productService;
    }

    @ResponseStatus(code=HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, value = "/packages")
    public PackageResource create(@RequestBody ChangePackageRequest request) {
        validateProductIds(request);
        final var createdPackage = packageService.create(request.name(), request.description(), request.productIds());
        return convertModelToApiResource(createdPackage);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/packages/{id}")
    public PackageResource get(@PathVariable String id) {
         return Optional.of(id)
                .map(Long::parseLong)
                .flatMap(packageService::get)
                 .map(this::convertModelToApiResource)
                .orElseThrow(EntityNotFoundException::new);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/packages/{id}")
    public PackageResource update(@PathVariable String id, @RequestBody ChangePackageRequest request) {
        final var parsedId = Long.parseLong(id);
        validateProductIds(request);
        final var updatedPackage = packageService.update(
                parsedId,
                request.name(),
                request.description(),
                request.productIds()
        );
        return convertModelToApiResource(updatedPackage);
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
                .map(this::convertModelToApiResource)
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

    private void validateProductIds(final ChangePackageRequest request) {
        final var anyInvalid = request.productIds()
                .stream()
                .map(this.productService::lookup)
                .anyMatch(Optional::isEmpty);
        if(anyInvalid) throw new UnknownProductId();
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseBody
    private ErrorResponse handleNotFound(final HttpServletRequest req, final Exception ex){
        return new ErrorResponse("package not found");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UnknownProductId.class)
    @ResponseBody
    private ErrorResponse handleUnknownProductId(final HttpServletRequest req, final Exception ex){
        return new ErrorResponse("unknown product ID");
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

    private PackageResource convertModelToApiResource(final Package model) {
        final var totalPrice = model.productIds().stream()
                .map(productId -> {
                    // TODO: confirm business requirements for this case.
                    final var productCost = this.productService.lookup(productId).map(Product::usdPrice);
                    if(productCost.isEmpty()) {
                        log.error(String.format("Unexpected error: package ID %d contains unknown product ID %s", model.id(), productId));
                    }
                    return productCost.orElse(0);
                })
                .reduce(Integer::sum)
                .orElse(0);

        return PackageResource.builder()
                .id(model.id())
                .name(model.name())
                .productIds(model.productIds())
                .description(model.description())
                .totalPrice(totalPrice)
                .build();
    }
}
