package com.lopreti.ledger.flow.ledger.domain;

import com.lopreti.ledger.flow.shared.domain.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Transaction {

    private final TransactionId id;
    private final Currency baseCurrency;
    private final Instant createdAt;

    private final List<Posting> postings;

    private TransactionStatus status;

    private Transaction(
            TransactionId id,
            Currency baseCurrency,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(
                id,
                "Transaction ID cannot be null"
        );

        this.baseCurrency = Objects.requireNonNull(
                baseCurrency,
                "Base currency cannot be null"
        );

        this.createdAt = Objects.requireNonNull(
                createdAt,
                "Created at cannot be null"
        );

        this.postings = new ArrayList<>();

        this.status = TransactionStatus.PENDING;
    }

    public static Transaction create(
            TransactionId id,
            Currency baseCurrency,
            Instant createdAt
    ) {
        return new Transaction(
                id,
                baseCurrency,
                createdAt
        );
    }

    public static Transaction reconstitute(
            TransactionId id,
            Currency baseCurrency,
            Instant createdAt,
            TransactionStatus status,
            List<Posting> postings
    ) {
        Transaction transaction = new Transaction(
                id,
                baseCurrency,
                createdAt
        );

        transaction.status = Objects.requireNonNull(
                status,
                "Transaction status cannot be null"
        );

        transaction.postings.addAll(
                Objects.requireNonNull(
                        postings,
                        "Postings cannot be null"
                )
        );

        return transaction;
    }

    public void addPosting(Posting posting) {
        ensurePending();

        Objects.requireNonNull(
                posting,
                "Posting cannot be null"
        );

        if (postings.stream()
                .anyMatch(existing ->
                        existing.id().equals(posting.id()))) {

            throw new IllegalArgumentException(
                    "Posting already belongs to this transaction"
            );
        }

        if (!posting.baseMoney()
                .currency()
                .equals(baseCurrency)) {

            throw new IllegalArgumentException(
                    "Posting base currency must match transaction base currency"
            );
        }

        postings.add(posting);
    }

    public void post() {
        ensurePending();

        if (postings.size() < 2) {
            throw new IllegalStateException(
                    "A transaction must contain at least two postings"
            );
        }

        validateBalance();

        status = TransactionStatus.POSTED;
    }

    public void reverse() {
        if (status != TransactionStatus.POSTED) {
            throw new IllegalStateException(
                    "Only posted transactions can be reversed"
            );
        }

        status = TransactionStatus.REVERSED;
    }

    private void validateBalance() {

        BigDecimal debits = postings.stream()
                .filter(posting ->
                        posting.type() == PostingType.DEBIT)
                .map(posting ->
                        posting.baseMoney().amount())
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        BigDecimal credits = postings.stream()
                .filter(posting ->
                        posting.type() == PostingType.CREDIT)
                .map(posting ->
                        posting.baseMoney().amount())
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        if (debits.compareTo(credits) != 0) {
            throw new IllegalStateException(
                    "Transaction is not balanced for base currency "
                            + baseCurrency.code()
            );
        }
    }

    private void ensurePending() {
        if (status != TransactionStatus.PENDING) {
            throw new IllegalStateException(
                    "Transaction cannot be modified after posting"
            );
        }
    }

    public TransactionId id() {
        return id;
    }

    public Currency baseCurrency() {
        return baseCurrency;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public TransactionStatus status() {
        return status;
    }

    public List<Posting> postings() {
        return Collections.unmodifiableList(postings);
    }
}
