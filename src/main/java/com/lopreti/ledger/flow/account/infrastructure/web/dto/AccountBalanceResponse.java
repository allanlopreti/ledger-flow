package com.lopreti.ledger.flow.account.infrastructure.web.dto;

import com.lopreti.ledger.flow.account.domain.AccountBalance;

import java.math.BigDecimal;

public record AccountBalanceResponse(
        BigDecimal balance,
        String currency
) {

    public static AccountBalanceResponse from(
            AccountBalance accountBalance
    ) {
        return new AccountBalanceResponse(
                accountBalance.amount(),
                accountBalance.currency().code()
        );
    }
}