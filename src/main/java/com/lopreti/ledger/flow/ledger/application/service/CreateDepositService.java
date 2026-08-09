package com.lopreti.ledger.flow.ledger.application.service;

import com.lopreti.ledger.flow.account.application.port.out.AccountRepository;
import com.lopreti.ledger.flow.account.domain.Account;
import com.lopreti.ledger.flow.account.domain.AccountBalance;
import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.ledger.application.port.in.CreateDepositUseCase;
import com.lopreti.ledger.flow.shared.domain.Currency;
import com.lopreti.ledger.flow.shared.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@Transactional
public class CreateDepositService
        implements CreateDepositUseCase {

    private final AccountRepository accountRepository;

    public CreateDepositService(
            AccountRepository accountRepository
    ) {
        this.accountRepository =
                Objects.requireNonNull(accountRepository);
    }

    @Override
    public AccountBalance execute(
            AccountId accountId,
            BigDecimal amount,
            Currency currency
    ) {

        Account account =
                accountRepository
                        .findById(accountId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found: "
                                                + accountId.value()
                                )
                        );

        if (!account.currency().equals(currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch"
            );
        }

        account.credit(
                Money.of(amount, currency)
        );

        accountRepository.save(account);

        return account.balance();
    }
}