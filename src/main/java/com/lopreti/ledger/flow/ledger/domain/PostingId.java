package com.lopreti.ledger.flow.ledger.domain;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.Objects;
import java.util.UUID;

public record PostingId(UUID value) {

    public PostingId {
        Objects.requireNonNull(value, "Posting ID cannot be null");
    }

    public static PostingId generate() {
        return new PostingId(UuidCreator.getTimeOrderedEpoch());
    }

    public static PostingId of(UUID value) {
        return new PostingId(value);
    }
}