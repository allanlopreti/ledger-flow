package com.lopreti.ledger.flow.ledger.application.service;

import com.lopreti.ledger.flow.account.application.port.out.AccountRepository;
import com.lopreti.ledger.flow.account.domain.Account;
import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.ledger.application.port.in.CreateFxTransactionUseCase;
import com.lopreti.ledger.flow.ledger.application.port.out.TransactionRepository;
import com.lopreti.ledger.flow.ledger.domain.Posting;
import com.lopreti.ledger.flow.ledger.domain.PostingId;
import com.lopreti.ledger.flow.ledger.domain.Transaction;
import com.lopreti.ledger.flow.ledger.domain.TransactionId;
import com.lopreti.ledger.flow.shared.domain.ExchangeRate;
import com.lopreti.ledger.flow.shared.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Service
@Transactional
public class CreateFxTransactionService
        implements CreateFxTransactionUseCase {

    private static final int MONEY_SCALE = 2;

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final Clock clock;

    public CreateFxTransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            Clock clock
    ) {
        this.transactionRepository =
                Objects.requireNonNull(transactionRepository);

        this.accountRepository =
                Objects.requireNonNull(accountRepository);

        this.clock =
                Objects.requireNonNull(clock);
    }

    @Override
    public Transaction execute(
            AccountId debitAccountId,
            Money debitMoney,
            AccountId creditAccountId,
            ExchangeRate exchangeRate
    ) {

        Objects.requireNonNull(
                debitAccountId,
                "Debit account ID cannot be null"
        );

        Objects.requireNonNull(
                debitMoney,
                "Debit money cannot be null"
        );

        Objects.requireNonNull(
                creditAccountId,
                "Credit account ID cannot be null"
        );

        Objects.requireNonNull(
                exchangeRate,
                "Exchange rate cannot be null"
        );

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

        if (!debitAccount.currency()
                .equals(exchangeRate.from())) {

            throw new IllegalArgumentException(
                    "Debit account currency does not match exchange rate source currency"
            );
        }

        if (!creditAccount.currency()
                .equals(exchangeRate.to())) {

            throw new IllegalArgumentException(
                    "Credit account currency does not match exchange rate target currency"
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

        debitAccount.debit(debitMoney);
        creditAccount.credit(convertedMoney);

        Instant now = Instant.now(clock);

        Transaction transaction =
                Transaction.createFx(
                        TransactionId.generate(),
                        exchangeRate.to(),
                        now,
                        exchangeRate
                );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        debitAccountId,
                        debitMoney,
                        convertedMoney,
                        now
                )
        );

        transaction.addPosting(
                Posting.credit(
                        PostingId.generate(),
                        creditAccountId,
                        convertedMoney,
                        convertedMoney,
                        now
                )
        );

        transaction.post();

        accountRepository.save(debitAccount);
        accountRepository.save(creditAccount);

        return transactionRepository.save(transaction);
    }
}