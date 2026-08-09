package com.lopreti.ledger.flow.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money money) {
        ensureSameCurrency(money);
        return new Money(
                amount.add(money.amount),
                currency
        );
    }

    public Money subtract(Money money) {
        ensureSameCurrency(money);
        return new Money(
                amount.subtract(money.amount),
                currency
        );
    }

    public Money multiply(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "Multiplier cannot be null");
        return new Money(
                amount.multiply(multiplier),
                currency
        );
    }

    public Money negate() {
        return new Money(
                amount.negate(),
                currency
        );
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public Money round(int scale) {
        return new Money(
                amount.setScale(scale, RoundingMode.HALF_EVEN),
                currency
        );
    }

    private void ensureSameCurrency(Money money) {
        Objects.requireNonNull(money, "Money cannot be null");
        if (!currency.equals(money.currency)) {
            throw new IllegalArgumentException(
                    "Cannot operate with different currencies"
            );
        }
    }

}
