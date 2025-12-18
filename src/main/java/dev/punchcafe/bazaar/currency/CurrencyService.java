package dev.punchcafe.bazaar.currency;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
public class CurrencyService {
    public final static String USD_CURRENCY_LABEL = "USD";

    private final CurrencyRatesCache currencyRatesCache;

    public CurrencyService(final CurrencyRatesCache currencyRatesCache){
        this.currencyRatesCache = currencyRatesCache;
    }

    public Optional<Double> convertUSDTo(final double usd, final String currency){
        final var currencyUppercase = currency.toUpperCase(Locale.ROOT);
        if(USD_CURRENCY_LABEL.equals(currencyUppercase)) {
            return Optional.of(usd);
        }

        final var rate = this.currencyRatesCache.fetchRates()
                .rates()
                .get(currencyUppercase);

        if(rate == null){
            return Optional.empty();
        }
        return Optional.of(usd * rate);
    }
}
