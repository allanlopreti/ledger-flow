package com.lopreti.ledger.flow.shared.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeRateTest {

    @Test
    void shouldConvertMoney() {
        var usd = Currency.of("USD");
        var brl = Currency.of("BRL");

        var rate = new ExchangeRate(
                usd,
                brl,
                new BigDecimal("5.20")
        );

        var money = Money.of(
                new BigDecimal("100.00"),
                usd
        );

        var converted = rate.convert(money, 2);

        assertEquals(
                new BigDecimal("520.00"),
                converted.amount()
        );

        assertEquals(brl, converted.currency());
    }

    @Test
    void shouldRejectZeroRate() {
        var usd = Currency.of("USD");
        var brl = Currency.of("BRL");

        assertThrows(
                IllegalArgumentException.class,
                () -> new ExchangeRate(
                        usd,
                        brl,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldRejectNegativeRate() {
        var usd = Currency.of("USD");
        var brl = Currency.of("BRL");

        assertThrows(
                IllegalArgumentException.class,
                () -> new ExchangeRate(
                        usd,
                        brl,
                        new BigDecimal("-1")
                )
        );
    }

    @Test
    void shouldRejectSameCurrency() {
        var usd = Currency.of("USD");

        assertThrows(
                IllegalArgumentException.class,
                () -> new ExchangeRate(
                        usd,
                        usd,
                        BigDecimal.ONE
                )
        );
    }

    @Test
    void shouldRejectMoneyWithWrongCurrency() {
        var usd = Currency.of("USD");
        var brl = Currency.of("BRL");

        var rate = new ExchangeRate(
                usd,
                brl,
                new BigDecimal("5.20")
        );

        var money = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> rate.convert(money, 2)
        );
    }
}