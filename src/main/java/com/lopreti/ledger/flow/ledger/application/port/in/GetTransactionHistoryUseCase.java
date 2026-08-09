package com.lopreti.ledger.flow.ledger.application.port.in;

import com.lopreti.ledger.flow.ledger.domain.Transaction;

import java.util.List;

public interface GetTransactionHistoryUseCase {

    List<Transaction> execute();
}
