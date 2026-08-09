package com.lopreti.ledger.flow.account.domain;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.Objects;
import java.util.UUID;

public record AccountId(UUID value) {

    public AccountId {
        Objects.requireNonNull(value, "Account ID cannot be null");
    }

    public static AccountId generate() {
        return new AccountId(UuidCreator.getTimeOrderedEpoch());
    }

    public static AccountId of(UUID value) {
        return new AccountId(value);
    }

}
