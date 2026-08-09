package com.lopreti.ledger.flow.ledger.infrastructure.web.dto;

import com.lopreti.ledger.flow.ledger.domain.Posting;
import com.lopreti.ledger.flow.ledger.domain.PostingType;
import com.lopreti.ledger.flow.ledger.domain.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionHistoryResponse(
        UUID transactionId,
        UUID debitAccountId,
        UUID creditAccountId,
        BigDecimal debitAmount,
        BigDecimal creditAmount,
        String debitCurrency,
        String creditCurrency,
        Instant createdAt
) {

    public static TransactionHistoryResponse from(
            Transaction transaction
    ) {

        Posting debit = transaction.postings()
                .stream()
                .filter(posting ->
                        posting.type() == PostingType.DEBIT
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Transaction does not contain a debit posting"
                        )
                );

        Posting credit = transaction.postings()
                .stream()
                .filter(posting ->
                        posting.type() == PostingType.CREDIT
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Transaction does not contain a credit posting"
                        )
                );

        return new TransactionHistoryResponse(
                transaction.id().value(),
                debit.accountId().value(),
                credit.accountId().value(),
                debit.money().amount(),
                credit.money().amount(),
                debit.money().currency().code(),
                credit.money().currency().code(),
                transaction.createdAt()
        );
    }
}