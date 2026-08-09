package com.lopreti.ledger.flow.ledger.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionRequest(

        @NotNull
        UUID debitAccountId,

        @NotNull
        @Positive
        BigDecimal debitAmount,

        @NotNull
        @Positive
        BigDecimal debitFunctionalAmount,

        @NotNull
        UUID creditAccountId,

        @NotNull
        @Positive
        BigDecimal creditAmount,

        @NotNull
        @Positive
        BigDecimal creditFunctionalAmount,

        @NotNull
        String debitCurrency,

        @NotNull
        String debitFunctionalCurrency,

        @NotNull
        String creditCurrency,

        @NotNull
        String creditFunctionalCurrency

) {
}