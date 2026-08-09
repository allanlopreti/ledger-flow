package com.lopreti.ledger.flow.ledger.application.port.out;

import com.lopreti.ledger.flow.ledger.domain.Transaction;
import com.lopreti.ledger.flow.ledger.domain.TransactionId;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    Transaction save(Transaction transaction);
    Optional<Transaction> findById(TransactionId id);
    boolean existsById(TransactionId id);
    List<Transaction> findAll();

}