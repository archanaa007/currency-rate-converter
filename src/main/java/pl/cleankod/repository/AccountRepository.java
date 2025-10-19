package pl.cleankod.repository;

import pl.cleankod.model.Account;

import java.util.Optional;

public interface AccountRepository {
    Optional<Account> find(Account.Id id);
    Optional<Account> find(Account.Number number);
}
