package dev.punchcafe.bazaar.packages;

import lombok.Builder;

import java.util.List;

@Builder
public record Package(long id, String name, String description, List<String> productIds) {

}
