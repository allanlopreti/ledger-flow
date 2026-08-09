package com.lopreti.ledger.flow.account.application.port.in;

import com.lopreti.ledger.flow.account.domain.Account;

import java.util.List;

public interface GetAccountsUseCase {

    List<Account> execute();
}