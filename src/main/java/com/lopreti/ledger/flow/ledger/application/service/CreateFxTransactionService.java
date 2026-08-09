package com.lopreti.ledger.flow.ledger.application.service;

import com.lopreti.ledger.flow.account.application.port.out.AccountRepository;
import com.lopreti.ledger.flow.account.domain.Account;
import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.ledger.application.port.in.CreateFxTransactionUseCase;
import com.lopreti.ledger.flow.ledger.application.port.in.CreateTransactionUseCase;
import com.lopreti.ledger.flow.ledger.domain.Transaction;
import com.lopreti.ledger.flow.shared.domain.ExchangeRate;
import com.lopreti.ledger.flow.shared.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class CreateFxTransactionService
        implements CreateFxTransactionUseCase {

    private static final int MONEY_SCALE = 2;

    private final CreateTransactionUseCase createTransactionUseCase;
    private final AccountRepository accountRepository;

    public CreateFxTransactionService(
            CreateTransactionUseCase createTransactionUseCase,
            AccountRepository accountRepository
    ) {
        this.createTransactionUseCase =
                Objects.requireNonNull(createTransactionUseCase);

        this.accountRepository =
                Objects.requireNonNull(accountRepository);
    }

    @Override
    public Transaction execute(
            AccountId debitAccountId,
            Money debitMoney,
            AccountId creditAccountId,
            ExchangeRate exchangeRate
    ) {

        Objects.requireNonNull(debitAccountId);
        Objects.requireNonNull(debitMoney);
        Objects.requireNonNull(creditAccountId);
        Objects.requireNonNull(exchangeRate);

        Account debitAccount =
                accountRepository.findById(debitAccountId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Debit account not found"
                                )
                        );

        Account creditAccount =
                accountRepository.findById(creditAccountId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Credit account not found"
                                )
                        );

        if (!debitAccount.currency()
                .equals(debitMoney.currency())) {

            throw new IllegalArgumentException(
                    "Debit account currency does not match debit money currency"
            );
        }

        if (!creditAccount.currency()
                .equals(exchangeRate.to())) {

            throw new IllegalArgumentException(
                    "Credit account currency does not match exchange rate target currency"
            );
        }

        if (!debitAccount.currency()
                .equals(exchangeRate.from())) {

            throw new IllegalArgumentException(
                    "Debit account currency does not match exchange rate source currency"
            );
        }

        Money convertedMoney =
                exchangeRate.convert(
                        debitMoney,
                        MONEY_SCALE
                );

        if (!convertedMoney.currency()
                .equals(creditAccount.currency())) {

            throw new IllegalArgumentException(
                    "Converted money currency does not match credit account currency"
            );
        }

        return createTransactionUseCase.execute(
                debitAccountId,
                debitMoney,
                convertedMoney,
                creditAccountId,
                convertedMoney,
                convertedMoney
        );
    }
}
