package com.lopreti.ledger.flow.shared.domain;

import java.util.Objects;

public record Currency(String code) {

    public Currency {
        Objects.requireNonNull(code, "Currency code cannot be null");
        if (!code.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Currency code must follow ISO 4217 format");
        }
    }

    public static Currency of(String code) {
        return new Currency(code);
    }

}
