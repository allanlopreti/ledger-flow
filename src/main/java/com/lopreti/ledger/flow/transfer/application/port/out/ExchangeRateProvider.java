package com.lopreti.ledger.flow.transfer.application.port.out;

import com.lopreti.ledger.flow.shared.domain.Currency;
import com.lopreti.ledger.flow.shared.domain.ExchangeRate;

public interface ExchangeRateProvider {

    ExchangeRate getRate(
            Currency from,
            Currency to
    );
}