package com.lopreti.ledger.flow.account.domain;

import com.lopreti.ledger.flow.shared.domain.Currency;

import java.math.BigDecimal;
import java.util.Objects;

public record AccountBalance(
        BigDecimal amount,
        Currency currency
) {

    public AccountBalance {

        Objects.requireNonNull(
                amount,
                "Balance amount cannot be null"
        );

        Objects.requireNonNull(
                currency,
                "Balance currency cannot be null"
        );

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Account balance cannot be negative"
            );
        }
    }

    public static AccountBalance zero(
            Currency currency
    ) {
        return new AccountBalance(
                BigDecimal.ZERO.setScale(4),
                currency
        );
    }

    public int compareTo(
            BigDecimal other
    ) {
        Objects.requireNonNull(
                other,
                "Amount cannot be null"
        );

        return amount.compareTo(other);
    }

    public AccountBalance add(
            BigDecimal value
    ) {
        Objects.requireNonNull(
                value,
                "Amount cannot be null"
        );

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Amount cannot be negative"
            );
        }

        return new AccountBalance(
                amount.add(value),
                currency
        );
    }

    public AccountBalance subtract(
            BigDecimal value
    ) {
        Objects.requireNonNull(
                value,
                "Amount cannot be null"
        );

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Amount cannot be negative"
            );
        }

        BigDecimal newAmount = amount.subtract(value);

        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "Insufficient account balance"
            );
        }

        return new AccountBalance(
                newAmount,
                currency
        );
    }
}
