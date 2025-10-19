package pl.cleankod.service.strategy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.cleankod.filter.TraceIdFilter;
import pl.cleankod.model.Account;
import pl.cleankod.service.usecase.FindAccountAndConvertCurrencyUseCase;
import pl.cleankod.service.usecase.FindAccountUseCase;
import java.util.Currency;
import java.util.Optional;

@Component
public class FindByNumberStrategy implements AccountRetrievalStrategy<Account.Number> {

    private static final Logger logger = LoggerFactory.getLogger(FindByNumberStrategy.class);
    private final FindAccountUseCase findAccountUseCase;
    private final FindAccountAndConvertCurrencyUseCase findAccountAndConvertCurrencyUseCase;

    public FindByNumberStrategy(FindAccountUseCase findAccountUseCase, FindAccountAndConvertCurrencyUseCase findAccountAndConvertCurrencyUseCase) {
        this.findAccountUseCase = findAccountUseCase;
        this.findAccountAndConvertCurrencyUseCase = findAccountAndConvertCurrencyUseCase;
    }

    @Override
    public Account findAccount(Account.Number number, String currency) {
        logger.info("Checking for expected currency for AccountNumber={}, currency={}, traceId={}", number, currency, TraceIdFilter.getCurrentTraceId());
        Optional<Account> account = StringUtils.isBlank(currency)
                ? findAccountUseCase.execute(number)
                : findAccountAndConvertCurrencyUseCase.execute(number, Currency.getInstance(currency));
        logger.info("Account Details received={}, traceId={}", account, TraceIdFilter.getCurrentTraceId());
        return account.orElse(null);
    }
}
