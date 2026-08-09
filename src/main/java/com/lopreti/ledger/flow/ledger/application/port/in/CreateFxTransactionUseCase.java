package com.lopreti.ledger.flow.ledger.application.port.in;

import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.ledger.domain.Transaction;
import com.lopreti.ledger.flow.shared.domain.ExchangeRate;
import com.lopreti.ledger.flow.shared.domain.Money;

public interface CreateFxTransactionUseCase {

    Transaction execute(
            AccountId debitAccountId,
            Money debitMoney,
            AccountId creditAccountId,
            ExchangeRate exchangeRate
    );
}