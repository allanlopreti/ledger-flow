package com.lopreti.ledger.flow.account.domain;

import com.lopreti.ledger.flow.shared.domain.Currency;

import java.time.Instant;
import java.util.Objects;

public final class Account {

    private final AccountId id;
    private final CustomerId customerId;
    private final Currency currency;
    private final Instant createdAt;

    private AccountStatus status;

    private Account(
            AccountId id,
            CustomerId customerId,
            Currency currency,
            AccountStatus status,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.customerId = Objects.requireNonNull(customerId);
        this.currency = Objects.requireNonNull(currency);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Account create(
            AccountId id,
            CustomerId customerId,
            Currency currency,
            Instant createdAt
    ) {
        return new Account(
                id,
                customerId,
                currency,
                AccountStatus.ACTIVE,
                createdAt
        );
    }

    public static Account reconstitute(
            AccountId id,
            CustomerId customerId,
            Currency currency,
            AccountStatus status,
            Instant createdAt
    ) {
        return new Account(
                id,
                customerId,
                currency,
                status,
                createdAt
        );
    }

    public void block() {
        if (status.equals(AccountStatus.CLOSED)) {
            throw new IllegalStateException("Closed account cannot be blocked");
        }
        status = AccountStatus.BLOCKED;
    }

    public void close() {
        if (status.equals(AccountStatus.CLOSED)) {
            throw new IllegalStateException("Account is already closed");
        }
        status = AccountStatus.CLOSED;
    }

    public void activate() {
        if (status.equals(AccountStatus.CLOSED)) {
            throw new IllegalStateException("Closed account cannot be activated");
        }
        status = AccountStatus.ACTIVE;
    }

    public AccountId id() {
        return id;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public Currency currency() {
        return currency;
    }

    public AccountStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

}
