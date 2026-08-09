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
    private final Money baseMoney;
    private final Instant createdAt;

    private Posting(
            PostingId id,
            AccountId accountId,
            PostingType type,
            Money money,
            Money baseMoney,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(
                id,
                "Posting ID cannot be null"
        );

        this.accountId = Objects.requireNonNull(
                accountId,
                "Account ID cannot be null"
        );

        this.type = Objects.requireNonNull(
                type,
                "Posting type cannot be null"
        );

        this.money = Objects.requireNonNull(
                money,
                "Money cannot be null"
        );

        this.baseMoney = Objects.requireNonNull(
                baseMoney,
                "Base money cannot be null"
        );

        this.createdAt = Objects.requireNonNull(
                createdAt,
                "Created at cannot be null"
        );

        if (!money.isPositive()) {
            throw new IllegalArgumentException(
                    "Posting amount must be positive"
            );
        }

        if (!baseMoney.isPositive()) {
            throw new IllegalArgumentException(
                    "Base money amount must be positive"
            );
        }
    }

    public static Posting debit(
            PostingId id,
            AccountId accountId,
            Money money,
            Money baseMoney,
            Instant createdAt
    ) {
        return new Posting(
                id,
                accountId,
                PostingType.DEBIT,
                money,
                baseMoney,
                createdAt
        );
    }

    public static Posting credit(
            PostingId id,
            AccountId accountId,
            Money money,
            Money baseMoney,
            Instant createdAt
    ) {
        return new Posting(
                id,
                accountId,
                PostingType.CREDIT,
                money,
                baseMoney,
                createdAt
        );
    }

    public static Posting reconstitute(
            PostingId id,
            AccountId accountId,
            PostingType type,
            Money money,
            Money baseMoney,
            Instant createdAt
    ) {
        return new Posting(
                id,
                accountId,
                type,
                money,
                baseMoney,
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

    public Money baseMoney() {
        return baseMoney;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
