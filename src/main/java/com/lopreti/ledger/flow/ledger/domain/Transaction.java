package com.lopreti.ledger.flow.ledger.domain;

import com.lopreti.ledger.flow.shared.domain.Currency;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Transaction {

    private final TransactionId id;
    private final Instant createdAt;

    private final List<Posting> postings;

    private TransactionStatus status;

    private Transaction(
            TransactionId id,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.postings = new ArrayList<>();
        this.status = TransactionStatus.PENDING;
    }

    public static Transaction create(
            TransactionId id,
            Instant createdAt
    ) {
        return new Transaction(id, createdAt);
    }

    public void addPosting(Posting posting) {
        ensurePending();

        Objects.requireNonNull(posting);

        if (postings.stream()
                .anyMatch(existing -> existing.id().equals(posting.id()))) {

            throw new IllegalArgumentException(
                    "Posting already belongs to this transaction"
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

        var currencies = postings.stream()
                .map(posting -> posting.money().currency())
                .distinct()
                .toList();

        for (Currency currency : currencies) {

            var debits = postings.stream()
                    .filter(posting ->
                            posting.type() == PostingType.DEBIT)
                    .filter(posting ->
                            posting.money()
                                    .currency()
                                    .equals(currency))
                    .map(posting ->
                            posting.money().amount())
                    .reduce(
                            java.math.BigDecimal.ZERO,
                            java.math.BigDecimal::add
                    );

            var credits = postings.stream()
                    .filter(posting ->
                            posting.type() == PostingType.CREDIT)
                    .filter(posting ->
                            posting.money()
                                    .currency()
                                    .equals(currency))
                    .map(posting ->
                            posting.money().amount())
                    .reduce(
                            java.math.BigDecimal.ZERO,
                            java.math.BigDecimal::add
                    );

            if (debits.compareTo(credits) != 0) {
                throw new IllegalStateException(
                        "Transaction is not balanced for currency "
                                + currency.code()
                );
            }
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