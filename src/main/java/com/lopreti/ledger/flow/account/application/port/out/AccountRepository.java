package com.lopreti.ledger.flow.account.application.port.out;

import com.lopreti.ledger.flow.account.domain.Account;
import com.lopreti.ledger.flow.account.domain.AccountId;

import java.util.Optional;

public interface AccountRepository {

    Account save(Account account);
    Optional<Account> findById(AccountId id);
    boolean existsById(AccountId id);

}
