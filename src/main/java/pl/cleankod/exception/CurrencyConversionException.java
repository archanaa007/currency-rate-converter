package pl.cleankod.exception;

import java.util.Currency;

public class CurrencyConversionException extends RuntimeException {
    public CurrencyConversionException(Currency sourceCurrency, Currency targetCurrency) {
        super(String.format("Cannot convert currency from %s to %s.", sourceCurrency, targetCurrency));
    }
    public CurrencyConversionException(String message) {
        super(message);
    }
    public CurrencyConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
