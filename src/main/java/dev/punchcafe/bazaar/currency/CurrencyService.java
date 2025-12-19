package dev.punchcafe.bazaar.currency;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

/**
 * Service for converting currencies from USD into other currencies.
 */
@Component
public class CurrencyService {
    public final static String USD_CURRENCY_LABEL = "USD";

    private final CurrencyRatesCache currencyRatesCache;

    public CurrencyService(final CurrencyRatesCache currencyRatesCache){
        this.currencyRatesCache = currencyRatesCache;
    }

    /**
     * Converts the given value in USD to the desired currency, i.e. GBP, JPY...
     * If the given currency isn't a known or supported currency, returns an empty Optional.
     *
     * @param usd the amount to convert in usd
     * @param currency the string currency code
     * @return the converted amount
     */
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
