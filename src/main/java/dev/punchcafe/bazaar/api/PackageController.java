package dev.punchcafe.bazaar.api;

import dev.punchcafe.bazaar.api.errors.UnknownProductId;
import dev.punchcafe.bazaar.currency.CurrencyService;
import dev.punchcafe.bazaar.packages.exceptions.DuplicateProductIdException;
import dev.punchcafe.bazaar.packages.exceptions.EntityNotFoundException;
import dev.punchcafe.bazaar.api.errors.InvalidPaginationParameters;
import dev.punchcafe.bazaar.api.schema.ChangePackageRequest;
import dev.punchcafe.bazaar.api.schema.ErrorResponse;
import dev.punchcafe.bazaar.api.schema.ListPackageResponse;
import dev.punchcafe.bazaar.api.schema.PackageResource;
import dev.punchcafe.bazaar.config.ApiConfiguration;
import dev.punchcafe.bazaar.packages.Package;
import dev.punchcafe.bazaar.packages.PackageService;
import dev.punchcafe.bazaar.products.Product;
import dev.punchcafe.bazaar.products.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

import static dev.punchcafe.bazaar.currency.CurrencyService.USD_CURRENCY_LABEL;


@Slf4j
@RestController
public class PackageController {

    private final ApiConfiguration apiConfiguration;
    private final PackageService packageService;
    private final ProductService productService;
    private final CurrencyService currencyService;

    public PackageController(
            final ApiConfiguration apiConfiguration,
            final PackageService packageService,
            final ProductService productService,
            final CurrencyService currencyService
            ) {
        this.apiConfiguration = apiConfiguration;
        this.packageService = packageService;
        this.productService = productService;
        this.currencyService = currencyService;
    }

    @ResponseStatus(code=HttpStatus.CREATED)
    @PostMapping(value = "/packages")
    public PackageResource create(@RequestBody ChangePackageRequest request) {
        validateProductIds(request);
        final var createdPackage = packageService.create(request.name(), request.description(), request.productIds());
        return convertModelToApiResource(createdPackage, USD_CURRENCY_LABEL);
    }

    @GetMapping(value = "/packages/{id}")
    public PackageResource get(
            @PathVariable String id,
            @RequestParam(value = "currency", defaultValue = USD_CURRENCY_LABEL) final String currency
    ) {
         return Optional.of(id)
                .map(Long::parseLong)
                .flatMap(packageService::get)
                 .map(existingPackage -> this.convertModelToApiResource(existingPackage, currency) )
                .orElseThrow(EntityNotFoundException::new);
    }

    @PutMapping(value = "/packages/{id}")
    public PackageResource update(@PathVariable String id, @RequestBody ChangePackageRequest request) {
        final var parsedId = Long.parseLong(id);
        validateProductIds(request);
        final var updatedPackage = packageService.update(
                parsedId,
                request.name(),
                request.description(),
                request.productIds()
        );
        return convertModelToApiResource(updatedPackage, USD_CURRENCY_LABEL);
    }

    @DeleteMapping(value = "/packages/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable String id) {
        final var parsedId = Long.parseLong(id);
        this.packageService.delete(parsedId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/packages")
    public ListPackageResponse list(
            @RequestParam(value = "page_size", defaultValue = "10") String pageSizeString,
            @RequestParam(value = "page_number", defaultValue = "0") String pageNumberString,
            @RequestParam(value = "currency", defaultValue = USD_CURRENCY_LABEL) String currency
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
                .map(pkg -> this.convertModelToApiResource(pkg, currency))
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
                .anyMatch(this::isInvalidProductId);
        if(anyInvalid) throw new UnknownProductId();
    }

    private boolean isInvalidProductId(final String productId) {
        if(Objects.isNull(productId)) {
            return true;
        }
        return this.productService.lookup(productId).isEmpty();
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
    @ExceptionHandler(DuplicateProductIdException.class)
    @ResponseBody
    private ErrorResponse handleDuplicateProductIdParams(final HttpServletRequest req, final Exception ex){
        return new ErrorResponse("productIds may not contain duplicates");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidPaginationParameters.class)
    @ResponseBody
    private ErrorResponse handleInvalidPaginationParams(final HttpServletRequest req, final Exception ex){
        return new ErrorResponse("invalid query pagination parameters");
    }

    private PackageResource convertModelToApiResource(final Package model, final String currency) {
        final var totalPriceUsd = model.productIds().stream()
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


        final var builder = PackageResource.builder()
                .id(model.id())
                .name(model.name())
                .productIds(model.productIds())
                .description(model.description());

        return addPrice(builder, currency, totalPriceUsd).build();
    }

    private PackageResource.PackageResourceBuilder addPrice(
            final PackageResource.PackageResourceBuilder builder,
            final String currency,
            final double usd) {
        final var convertedAmount = this.currencyService.convertUSDTo(usd, currency);
        if(convertedAmount.isPresent()) {
            // TODO: clean up repeats of upper case
            builder.totalPrice((float) (double) convertedAmount.get()).currency(currency.toUpperCase());
            return builder;
        } else {
            return builder.totalPrice((float) usd).currency(USD_CURRENCY_LABEL);
        }
    }
}
