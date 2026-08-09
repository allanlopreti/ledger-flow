package com.lopreti.ledger.flow.account.domain;

import com.lopreti.ledger.flow.shared.domain.Currency;
import com.lopreti.ledger.flow.shared.domain.Money;

import java.time.Instant;
import java.util.Objects;

public final class Account {

    private final AccountId id;
    private final CustomerId customerId;
    private final Currency currency;
    private final Instant createdAt;

    private AccountStatus status;
    private AccountBalance balance;

    private Account(
            AccountId id,
            CustomerId customerId,
            Currency currency,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(
                id,
                "Account ID cannot be null"
        );

        this.customerId = Objects.requireNonNull(
                customerId,
                "Customer ID cannot be null"
        );

        this.currency = Objects.requireNonNull(
                currency,
                "Currency cannot be null"
        );

        this.createdAt = Objects.requireNonNull(
                createdAt,
                "Created at cannot be null"
        );

        this.status = AccountStatus.ACTIVE;
        this.balance = AccountBalance.zero(currency);
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
                createdAt
        );
    }

    public static Account reconstitute(
            AccountId id,
            CustomerId customerId,
            Currency currency,
            AccountStatus status,
            Instant createdAt,
            AccountBalance balance
    ) {
        Account account = new Account(
                id,
                customerId,
                currency,
                createdAt
        );

        account.status = Objects.requireNonNull(
                status,
                "Account status cannot be null"
        );

        account.balance = Objects.requireNonNull(
                balance,
                "Account balance cannot be null"
        );

        return account;
    }

    public void block() {
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only active accounts can be blocked"
            );
        }

        status = AccountStatus.BLOCKED;
    }

    public void activate() {
        if (status == AccountStatus.ACTIVE) {
            return;
        }

        if (status != AccountStatus.BLOCKED) {
            throw new IllegalStateException(
                    "Only blocked accounts can be activated"
            );
        }

        status = AccountStatus.ACTIVE;
    }

    public void close() {
        if (status == AccountStatus.CLOSED) {
            throw new IllegalStateException(
                    "Account is already closed"
            );
        }

        status = AccountStatus.CLOSED;
    }

    public void debit(Money amount) {
        Objects.requireNonNull(
                amount,
                "Amount cannot be null"
        );

        ensureActive();

        if (!currency.equals(amount.currency())) {
            throw new IllegalArgumentException(
                    "Account currency must match debit currency"
            );
        }

        if (balance.amount().compareTo(amount.amount()) < 0) {
            throw new IllegalStateException(
                    "Insufficient account balance"
            );
        }

        balance = new AccountBalance(
                balance.amount().subtract(amount.amount()),
                currency
        );
    }

    public void credit(Money amount) {
        Objects.requireNonNull(
                amount,
                "Amount cannot be null"
        );

        ensureActive();

        if (!currency.equals(amount.currency())) {
            throw new IllegalArgumentException(
                    "Account currency must match credit currency"
            );
        }

        balance = new AccountBalance(
                balance.amount().add(amount.amount()),
                currency
        );
    }

    private void ensureActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Account must be active"
            );
        }
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

    public AccountBalance balance() {
        return balance;
    }
}