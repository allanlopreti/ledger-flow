package com.lopreti.ledger.flow.account.application.service;

import com.lopreti.ledger.flow.account.application.port.in.GetAccountBalanceUseCase;
import com.lopreti.ledger.flow.account.application.port.out.AccountRepository;
import com.lopreti.ledger.flow.account.domain.AccountBalance;
import com.lopreti.ledger.flow.account.domain.AccountId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class GetAccountBalanceService
        implements GetAccountBalanceUseCase {

    private final AccountRepository accountRepository;

    public GetAccountBalanceService(
            AccountRepository accountRepository
    ) {
        this.accountRepository =
                Objects.requireNonNull(accountRepository);
    }

    @Override
    public AccountBalance execute(
            AccountId accountId
    ) {

        return accountRepository
                .findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found: "
                                        + accountId.value()
                        )
                )
                .balance();
    }
}