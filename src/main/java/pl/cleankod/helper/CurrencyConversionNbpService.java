package pl.cleankod.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import pl.cleankod.exception.CurrencyConversionException;
import pl.cleankod.filter.TraceIdFilter;
import pl.cleankod.model.Money;
import pl.cleankod.exception.NbpApiException;
import pl.cleankod.cache.NbpRateCacheService;
import pl.cleankod.model.nbp.RateWrapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrencyConversionNbpService implements CurrencyConversionService {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyConversionNbpService.class);
    private final NbpRateCacheService nbpRateCacheService;

    @Autowired
    public CurrencyConversionNbpService(NbpRateCacheService nbpRateCacheService) {
      this.nbpRateCacheService = nbpRateCacheService;
    }

    @Override
    public Money convert(Money money, Currency targetCurrency) {
        try {
            logger.info("Converting money amount={}, to currency={}, traceId={}", money.amount(), targetCurrency, TraceIdFilter.getCurrentTraceId());
            RateWrapper rateWrapper = nbpRateCacheService.getRateWrapperFromNbpClient("A", targetCurrency.getCurrencyCode());
            logger.info("RateWrapper response received={}, traceId={}", rateWrapper, TraceIdFilter.getCurrentTraceId());
            validateRateWrapper(rateWrapper, targetCurrency);

            BigDecimal midRate = rateWrapper.rates().get(0).mid();
            if (midRate == null) {
                logger.error("Mid rate is missing for currency={}, traceId={}", targetCurrency.getCurrencyCode(), TraceIdFilter.getCurrentTraceId());
                throw new NbpApiException("Mid rate is missing for currency: " + targetCurrency.getCurrencyCode());
            }

            BigDecimal calculatedRate = safeDivide(money.amount(), midRate);
            BigDecimal roundedRate = calculatedRate.setScale(2, RoundingMode.HALF_UP);
            return new Money(roundedRate, targetCurrency);

        } catch (feign.FeignException fe) {
            throw mapFeignException(fe, targetCurrency);
        } catch (NbpApiException | CurrencyConversionException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error during currency conversion={}, traceId={}", ex.getMessage(), TraceIdFilter.getCurrentTraceId());
            throw new CurrencyConversionException("Unexpected error during currency conversion: " + ex.getMessage(), ex);
        }
    }

    private void validateRateWrapper(RateWrapper rateWrapper, Currency targetCurrency) {
        if (rateWrapper == null || CollectionUtils.isEmpty(rateWrapper.rates())) {
            throw new NbpApiException("No rates available for currency: " + targetCurrency.getCurrencyCode());
        }
    }

    private BigDecimal safeDivide(BigDecimal amount, BigDecimal midRate) {
        try {
            return amount.divide(midRate, 10, RoundingMode.HALF_UP);
        } catch (ArithmeticException | NullPointerException e) {
            throw new CurrencyConversionException("Error calculating conversion rate: " + e.getMessage(), e);
        }
    }

    private NbpApiException mapFeignException(feign.FeignException fe, Currency targetCurrency) {
        int status = fe.status();
        if (status == 404) {
            return new NbpApiException("Currency not found in NBP API: " + targetCurrency.getCurrencyCode(), fe);
        } else if (status == 429) {
            return new NbpApiException("Rate limit exceeded for NBP API.", fe);
        } else if (status >= 400 && status < 500) {
            return new NbpApiException("Client error from NBP API: " + fe.getMessage(), fe);
        } else if (status >= 500) {
            return new NbpApiException("Server error from NBP API: " + fe.getMessage(), fe);
        } else {
            return new NbpApiException("Unexpected error from NBP API: " + fe.getMessage(), fe);
        }
    }
    
}
