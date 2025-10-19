package pl.cleankod.service.impl;

import org.springframework.stereotype.Service;
import pl.cleankod.util.Result;
import pl.cleankod.model.Account;
import pl.cleankod.service.AccountService;
import pl.cleankod.service.strategy.AccountRetrievalStrategy;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRetrievalStrategy<Account.Id> findByIdStrategy;
    private final AccountRetrievalStrategy<Account.Number> findByNumberStrategy;

    public AccountServiceImpl(AccountRetrievalStrategy<Account.Id> findByIdStrategy,
                              AccountRetrievalStrategy<Account.Number> findByNumberStrategy) {
        this.findByIdStrategy = findByIdStrategy;
        this.findByNumberStrategy = findByNumberStrategy;
    }

    @Override
    public Result<Account, String> findAccountById(String id, String currency) {
        try {
            Account account = findByIdStrategy.findAccount(Account.Id.of(id), currency);
            if (account == null) {
                return Result.failure("Account not found for ID: " + id);
            }
            return Result.success(account);
        } catch (Exception ex) {
            return Result.failure("Error finding account by ID: " + ex.getMessage());
        }
    }

    @Override
    public Result<Account, String> findAccountByNumber(String number, String currency) {
        try {
            Account.Number accountNumber = Account.Number.of(URLDecoder.decode(number, StandardCharsets.UTF_8));
            Account account = findByNumberStrategy.findAccount(accountNumber, currency);
            if (account == null) {
                return Result.failure("Account not found for number: " + number);
            }
            return Result.success(account);
        } catch (Exception ex) {
            return Result.failure("Error finding account by number: " + ex.getMessage());
        }
    }
}
