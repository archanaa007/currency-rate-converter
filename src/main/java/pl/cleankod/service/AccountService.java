package pl.cleankod.service;

import pl.cleankod.util.Result;
import pl.cleankod.model.Account;

public interface AccountService {
    Result<Account, String> findAccountById(String id, String currency);

    Result<Account, String> findAccountByNumber(String number, String currency);
}
