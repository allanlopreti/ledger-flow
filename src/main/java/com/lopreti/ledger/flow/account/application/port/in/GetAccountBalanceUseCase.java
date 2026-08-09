package com.lopreti.ledger.flow.account.application.port.in;

import com.lopreti.ledger.flow.account.domain.AccountBalance;
import com.lopreti.ledger.flow.account.domain.AccountId;

public interface GetAccountBalanceUseCase {

    AccountBalance execute(AccountId accountId);
}