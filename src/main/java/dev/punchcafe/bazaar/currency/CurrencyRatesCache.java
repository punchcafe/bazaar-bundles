package dev.punchcafe.bazaar.currency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class CurrencyRatesCache {

    final RestTemplate restTemplate;

    public CurrencyRatesCache(final RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    // TODO: handle edge case failure here
    // TODO: prometheus gauge for date of latest currency?
    // TODO: add unit testing
    @Cacheable("currency-rates")
    public CurrencyApiResponse fetchRates() {
        // TODO: maybe extract url to env var?
        final var response = this.restTemplate.getForEntity(
                "https://api.frankfurter.dev/v1/latest?base=USD",
                CurrencyApiResponse.class
        );
        if((HttpStatus.OK != response.getStatusCode()) || !"USD".equals(response.getBody().base())) {
            log.error("failed to retrieve currency conversion rates");
            throw new RuntimeException("unable to get USD rates");
        }
        return response.getBody();
    }

    @CacheEvict("currency-rates")
    // Updated daily around CET 16:00, so give an hour extra for safety
    @Scheduled(cron = "0 17 * * *", zone = "Europe/Berlin")
    void evictRates() {}
}
