package com.lopreti.ledger.flow.account.domain;

import java.util.Objects;
import java.util.UUID;

public record CustomerId(UUID value) {

    public CustomerId {
        Objects.requireNonNull(value, "Customer ID cannot be null");
    }

}
