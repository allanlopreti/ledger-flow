package com.lopreti.ledger.flow.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record ExchangeRate(
        Currency from,
        Currency to,
        BigDecimal rate
) {

    public ExchangeRate {
        Objects.requireNonNull(from, "Source currency cannot be null");
        Objects.requireNonNull(to, "Target currency cannot be null");
        Objects.requireNonNull(rate, "Exchange rate cannot be null");

        if (from.equals(to)) {
            throw new IllegalArgumentException(
                    "Source and target currencies must be different"
            );
        }

        if (rate.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Exchange rate must be positive"
            );
        }
    }

    public Money convert(Money money, int scale) {
        Objects.requireNonNull(money, "Money cannot be null");

        if (!money.currency().equals(from)) {
            throw new IllegalArgumentException(
                    "Money currency does not match exchange rate source currency"
            );
        }

        return new Money(
                money.amount()
                        .multiply(rate)
                        .setScale(scale, RoundingMode.HALF_EVEN),
                to
        );
    }
}
