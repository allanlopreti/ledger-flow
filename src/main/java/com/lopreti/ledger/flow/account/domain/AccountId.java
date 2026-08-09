package com.lopreti.ledger.flow.account.domain;

import java.util.Objects;
import java.util.UUID;

public record AccountId(UUID value) {

    public AccountId {
        Objects.requireNonNull(value, "Account ID cannot be null");
    }

    public static AccountId generate() {
        return new AccountId(UUID.randomUUID());
    }

}
