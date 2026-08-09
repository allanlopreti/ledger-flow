package com.lopreti.ledger.flow.account.application.service;

import com.lopreti.ledger.flow.account.application.port.in.GetAccountsUseCase;
import com.lopreti.ledger.flow.account.application.port.out.AccountRepository;
import com.lopreti.ledger.flow.account.domain.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class GetAccountsService
        implements GetAccountsUseCase {

    private final AccountRepository accountRepository;

    public GetAccountsService(
            AccountRepository accountRepository
    ) {
        this.accountRepository =
                Objects.requireNonNull(accountRepository);
    }

    @Override
    public List<Account> execute() {
        return accountRepository.findAll();
    }
}