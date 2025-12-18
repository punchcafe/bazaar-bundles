package dev.punchcafe.bazaar.products;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * The product service manages read access to Product information.
 */
@Component
public class ProductService {
    final ProductsCache productsCache;

    public ProductService(final ProductsCache cache){
        this.productsCache = cache;
    }

    /**
     * Look up a given product by its ID.
     * If no product exists, returns empty.
     *
     * @param productId the product ID
     * @return the product, if it exists.
     */
    public Optional<Product> lookup(final String productId) {
        return Optional.ofNullable(this.productsCache.getCache().get(productId));
    }
}
