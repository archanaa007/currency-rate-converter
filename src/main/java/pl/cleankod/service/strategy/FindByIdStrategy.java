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
public class FindByIdStrategy implements AccountRetrievalStrategy<Account.Id> {
    
    private static final Logger logger = LoggerFactory.getLogger(FindByIdStrategy.class);
    private final FindAccountUseCase findAccountUseCase;
    private final FindAccountAndConvertCurrencyUseCase findAccountAndConvertCurrencyUseCase;

    public FindByIdStrategy(FindAccountUseCase findAccountUseCase, FindAccountAndConvertCurrencyUseCase findAccountAndConvertCurrencyUseCase) {
        this.findAccountUseCase = findAccountUseCase;
        this.findAccountAndConvertCurrencyUseCase = findAccountAndConvertCurrencyUseCase;
    }

    @Override
    public Account findAccount(Account.Id id, String currency) {
        logger.info("Checking for expected currency for AccountId={}, currency={}, traceId={}", id, currency, TraceIdFilter.getCurrentTraceId());
        Optional<Account> account = StringUtils.isBlank(currency)
                ? findAccountUseCase.execute(id)
                : findAccountAndConvertCurrencyUseCase.execute(id, Currency.getInstance(currency));
        logger.info("Account Details received={}, traceId={}", account, TraceIdFilter.getCurrentTraceId());
        // Return the Account if present, otherwise return null
        return account.orElse(null);
    }
}
