package com.lopreti.ledger.flow.shared.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyTest {

    @Test
    void shouldCreateValidCurrency() {
        Currency currency = Currency.of("BRL");
        assertEquals("BRL", currency.code());
    }

    @Test
    void shouldAcceptUsd() {
        Currency currency = Currency.of("USD");
        assertEquals("USD", currency.code());
    }

    @Test
    void shouldAcceptEur() {
        Currency currency = Currency.of("EUR");
        assertEquals("EUR", currency.code());
    }

    @Test
    void shouldRejectNullCurrency() {
        assertThrows(
                NullPointerException.class,
                () -> Currency.of(null)
        );
    }

    @Test
    void shouldRejectLowercaseCurrency() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Currency.of("usd")
        );
    }

    @Test
    void shouldRejectInvalidCurrencyLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Currency.of("BR")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> Currency.of("BRLL")
        );
    }

    @Test
    void shouldRejectNumericCurrency() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Currency.of("123")
        );
    }
}