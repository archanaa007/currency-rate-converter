package pl.cleankod.service.strategy;

import pl.cleankod.model.Account;

public interface AccountRetrievalStrategy<T> {
    Account findAccount(T identifier, String currency);
}

