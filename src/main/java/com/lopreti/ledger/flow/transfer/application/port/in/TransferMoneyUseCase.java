package com.lopreti.ledger.flow.transfer.application.port.in;

import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.shared.domain.Money;

public interface TransferMoneyUseCase {

    void transfer(
            AccountId sourceAccountId,
            AccountId targetAccountId,
            Money amount
    );
}