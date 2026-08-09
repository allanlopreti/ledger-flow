package com.lopreti.ledger.flow.account.application.service;

import com.lopreti.ledger.flow.account.application.port.in.CreateAccountUseCase;
import com.lopreti.ledger.flow.account.application.port.out.AccountRepository;
import com.lopreti.ledger.flow.account.domain.Account;
import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.account.domain.CustomerId;
import com.lopreti.ledger.flow.shared.domain.Currency;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Service
@Transactional
public class CreateAccountService implements CreateAccountUseCase {

    private final AccountRepository accountRepository;
    private final Clock clock;

    public CreateAccountService(
            AccountRepository accountRepository,
            Clock clock
    ) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public Account execute(
            CustomerId customerId,
            Currency currency
    ) {
        AccountId accountId = AccountId.generate();

        Account account = Account.create(
                accountId,
                customerId,
                currency,
                Instant.now(clock)
        );

        return accountRepository.save(account);
    }
}