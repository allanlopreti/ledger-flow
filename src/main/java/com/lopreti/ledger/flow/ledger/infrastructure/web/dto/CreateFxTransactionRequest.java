package com.lopreti.ledger.flow.ledger.infrastructure.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateFxTransactionRequest(
        UUID debitAccountId,
        BigDecimal debitAmount,
        String debitCurrency,
        UUID creditAccountId,
        String creditCurrency,
        BigDecimal exchangeRate
) {
}