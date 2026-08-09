package com.lopreti.ledger.flow.ledger.application.service;

import com.lopreti.ledger.flow.account.application.port.out.AccountRepository;
import com.lopreti.ledger.flow.account.domain.Account;
import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.ledger.application.port.in.CreateTransactionUseCase;
import com.lopreti.ledger.flow.ledger.application.port.out.TransactionRepository;
import com.lopreti.ledger.flow.ledger.domain.Posting;
import com.lopreti.ledger.flow.ledger.domain.PostingId;
import com.lopreti.ledger.flow.ledger.domain.Transaction;
import com.lopreti.ledger.flow.ledger.domain.TransactionId;
import com.lopreti.ledger.flow.shared.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Service
@Transactional
public class CreateTransactionService
        implements CreateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final Clock clock;

    public CreateTransactionService(
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
            Money debitFunctionalMoney,
            AccountId creditAccountId,
            Money creditMoney,
            Money creditFunctionalMoney
    ) {
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

        debitAccount.debit(debitMoney);
        creditAccount.credit(creditMoney);

        Instant now = Instant.now(clock);

        Transaction transaction = Transaction.create(
                TransactionId.generate(),
                debitFunctionalMoney.currency(),
                now
        );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        debitAccountId,
                        debitMoney,
                        debitFunctionalMoney,
                        now
                )
        );

        transaction.addPosting(
                Posting.credit(
                        PostingId.generate(),
                        creditAccountId,
                        creditMoney,
                        creditFunctionalMoney,
                        now
                )
        );

        transaction.post();

        accountRepository.save(debitAccount);
        accountRepository.save(creditAccount);

        return transactionRepository.save(transaction);
    }
}