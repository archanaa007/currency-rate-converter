package pl.cleankod.cache;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import pl.cleankod.exception.NbpApiException;
import pl.cleankod.remote.ExchangeRatesNbpClient;
import pl.cleankod.model.nbp.RateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import pl.cleankod.filter.TraceIdFilter;

public class NbpRateCacheService {
    private static final Logger logger = LoggerFactory.getLogger(NbpRateCacheService.class);
    private final ExchangeRatesNbpClient exchangeRatesNbpClient;

    public NbpRateCacheService(ExchangeRatesNbpClient exchangeRatesNbpClient) {
        this.exchangeRatesNbpClient = exchangeRatesNbpClient;
    }

    @Cacheable("nbpRates")
    @Retryable(
        value = { feign.FeignException.class, pl.cleankod.exception.NbpApiException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    @CircuitBreaker(name = "nbpApi", fallbackMethod = "fallbackRateWrapper")
    public RateWrapper getRateWrapperFromNbpClient(String table, String currencyCode) throws NbpApiException {
        logger.info("Cache MISS: Fetching rate from NBP API for table={}, currencyCode={}, traceId={}", table, currencyCode, TraceIdFilter.getCurrentTraceId());
        return exchangeRatesNbpClient.fetch(table, currencyCode);
    }

    public RateWrapper fallbackRateWrapper(String table, String currencyCode, Throwable t) throws NbpApiException {
        logger.error("Circuit breaker fallback triggered for table={}, currencyCode={}. Reason: {}", table, currencyCode, t.getMessage());
        throw new NbpApiException("NBP API is currently unavailable. Please try again later.", t);
    }
}
