package com.lopreti.ledger.flow.account.application.port.in;

import com.lopreti.ledger.flow.account.domain.Account;
import com.lopreti.ledger.flow.account.domain.CustomerId;
import com.lopreti.ledger.flow.shared.domain.Currency;

public interface CreateAccountUseCase {

    Account execute(
            CustomerId customerId,
            Currency currency
    );
}