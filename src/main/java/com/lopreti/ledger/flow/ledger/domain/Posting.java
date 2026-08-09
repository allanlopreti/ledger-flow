package com.lopreti.ledger.flow.ledger.domain;

import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.shared.domain.Money;

import java.time.Instant;
import java.util.Objects;

public final class Posting {

    private final PostingId id;
    private final AccountId accountId;
    private final PostingType type;
    private final Money money;
    private final Instant createdAt;

    private Posting(
            PostingId id,
            AccountId accountId,
            PostingType type,
            Money money,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.accountId = Objects.requireNonNull(accountId);
        this.type = Objects.requireNonNull(type);
        this.money = Objects.requireNonNull(money);
        this.createdAt = Objects.requireNonNull(createdAt);

        if (!money.isPositive()) {
            throw new IllegalArgumentException(
                    "Posting amount must be positive"
            );
        }
    }

    public static Posting debit(
            PostingId id,
            AccountId accountId,
            Money money,
            Instant createdAt
    ) {
        return new Posting(
                id,
                accountId,
                PostingType.DEBIT,
                money,
                createdAt
        );
    }

    public static Posting credit(
            PostingId id,
            AccountId accountId,
            Money money,
            Instant createdAt
    ) {
        return new Posting(
                id,
                accountId,
                PostingType.CREDIT,
                money,
                createdAt
        );
    }

    public PostingId id() {
        return id;
    }

    public AccountId accountId() {
        return accountId;
    }

    public PostingType type() {
        return type;
    }

    public Money money() {
        return money;
    }

    public Instant createdAt() {
        return createdAt;
    }

}