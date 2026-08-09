package com.lopreti.ledger.flow.shared.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldAddMoneyWithSameCurrency() {
        Money first = Money.of(
                new BigDecimal("100.00"),
                Currency.of("BRL")
        );

        Money second = Money.of(
                new BigDecimal("50.00"),
                Currency.of("BRL")
        );

        Money result = first.add(second);

        assertEquals(
                new BigDecimal("150.00"),
                result.amount()
        );
    }

    @Test
    void shouldNotAddDifferentCurrencies() {
        Money brl = Money.of(
                new BigDecimal("100.00"),
                Currency.of("BRL")
        );

        Money usd = Money.of(
                new BigDecimal("100.00"),
                Currency.of("USD")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> brl.add(usd)
        );
    }

}
