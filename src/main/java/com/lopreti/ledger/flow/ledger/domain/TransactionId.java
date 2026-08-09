package com.lopreti.ledger.flow.ledger.domain;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.Objects;
import java.util.UUID;

public record TransactionId(UUID value) {

    public TransactionId {
        Objects.requireNonNull(
                value,
                "Transaction ID cannot be null"
        );
    }

    public static TransactionId generate() {
        return new TransactionId(
                UuidCreator.getTimeOrderedEpoch()
        );
    }

    public static TransactionId of(UUID value) {
        return new TransactionId(value);
    }
}