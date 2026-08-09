package com.lopreti.ledger.flow.account.domain;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.Objects;
import java.util.UUID;

public record CustomerId(UUID value) {

    public CustomerId {
        Objects.requireNonNull(value, "Customer ID cannot be null");
    }

    public static CustomerId generate() {
        return new CustomerId(UuidCreator.getTimeOrderedEpoch());
    }

    public static CustomerId of(UUID value) {
        return new CustomerId(value);
    }

}
