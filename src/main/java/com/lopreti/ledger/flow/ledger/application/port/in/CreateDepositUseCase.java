package com.lopreti.ledger.flow.ledger.application.port.in;

import com.lopreti.ledger.flow.account.domain.AccountBalance;
import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.shared.domain.Currency;

import java.math.BigDecimal;

public interface CreateDepositUseCase {

    AccountBalance execute(
            AccountId accountId,
            BigDecimal amount,
            Currency currency
    );
}