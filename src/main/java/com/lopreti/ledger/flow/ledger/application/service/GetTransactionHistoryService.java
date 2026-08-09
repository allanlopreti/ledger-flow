package com.lopreti.ledger.flow.ledger.application.service;

import com.lopreti.ledger.flow.ledger.application.port.in.GetTransactionHistoryUseCase;
import com.lopreti.ledger.flow.ledger.application.port.out.TransactionRepository;
import com.lopreti.ledger.flow.ledger.domain.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class GetTransactionHistoryService
        implements GetTransactionHistoryUseCase {

    private final TransactionRepository transactionRepository;

    public GetTransactionHistoryService(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository =
                Objects.requireNonNull(transactionRepository);
    }

    @Override
    public List<Transaction> execute() {
        return transactionRepository.findAll();
    }
}

