package com.lopreti.ledger.flow.account.infrastructure.web.dto;

import com.lopreti.ledger.flow.account.domain.Account;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID customerId,
        String currency,
        String status,
        Instant createdAt
) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.id().value(),
                account.customerId().value(),
                account.currency().code(),
                account.status().name(),
                account.createdAt()
        );
    }
}