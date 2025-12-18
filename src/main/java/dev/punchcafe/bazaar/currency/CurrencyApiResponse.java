package dev.punchcafe.bazaar.currency;

import java.util.Map;

public record CurrencyApiResponse(String base, String date, Map<String, Double> rates) {
}
