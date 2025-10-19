package pl.cleankod.helper;

import pl.cleankod.model.Money;

import java.util.Currency;

public interface CurrencyConversionService {
    Money convert(Money money, Currency targetCurrency);
}
