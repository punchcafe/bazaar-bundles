package dev.punchcafe.bazaar.api.schema;

import lombok.Builder;
import lombok.NonNull;

import java.util.List;

@Builder
public record ChangePackageRequest(String name, String description, @NonNull List<String> productIds) {
}
