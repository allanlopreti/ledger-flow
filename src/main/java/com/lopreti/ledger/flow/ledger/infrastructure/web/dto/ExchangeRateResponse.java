package com.lopreti.ledger.flow.ledger.infrastructure.web.dto;

import com.lopreti.ledger.flow.shared.domain.ExchangeRate;

import java.math.BigDecimal;

public record ExchangeRateResponse(
        String from,
        String to,
        BigDecimal rate
) {

    public static ExchangeRateResponse from(
            ExchangeRate exchangeRate
    ) {
        if (exchangeRate == null) {
            return null;
        }

        return new ExchangeRateResponse(
                exchangeRate.from().code(),
                exchangeRate.to().code(),
                exchangeRate.rate()
        );
    }
}
