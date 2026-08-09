package com.lopreti.ledger.flow.ledger.application.service;

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
    private final Clock clock;

    public CreateTransactionService(
            TransactionRepository transactionRepository,
            Clock clock
    ) {
        this.transactionRepository =
                Objects.requireNonNull(transactionRepository);

        this.clock =
                Objects.requireNonNull(clock);
    }

    @Override
    public Transaction execute(
            AccountId debitAccountId,
            Money debitMoney,
            Money debitBaseMoney,
            AccountId creditAccountId,
            Money creditMoney,
            Money creditBaseMoney
    ) {
        Instant now = Instant.now(clock);

        Transaction transaction = Transaction.create(
                TransactionId.generate(),
                debitBaseMoney.currency(),
                now
        );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        debitAccountId,
                        debitMoney,
                        debitBaseMoney,
                        now
                )
        );

        transaction.addPosting(
                Posting.credit(
                        PostingId.generate(),
                        creditAccountId,
                        creditMoney,
                        creditBaseMoney,
                        now
                )
        );

        transaction.post();

        return transactionRepository.save(transaction);
    }

}