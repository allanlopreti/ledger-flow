package com.lopreti.ledger.flow.ledger.infrastructure.web.dto;

import com.lopreti.ledger.flow.ledger.domain.Posting;
import com.lopreti.ledger.flow.ledger.domain.Transaction;
import com.lopreti.ledger.flow.shared.domain.ExchangeRate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String baseCurrency,
        String status,
        Instant createdAt,
        ExchangeRateResponse exchangeRate,
        List<PostingResponse> postings
) {
    public static TransactionResponse from(
            Transaction transaction
    ) {
        return new TransactionResponse(
                transaction.id().value(),
                transaction.baseCurrency().code(),
                transaction.status().name(),
                transaction.createdAt(),
                ExchangeRateResponse.from(transaction.exchangeRate()),
                transaction.postings()
                        .stream()
                        .map(PostingResponse::from)
                        .toList()
        );
    }

    public record PostingResponse(
            UUID id,
            UUID accountId,
            String type,
            BigDecimal amount,
            String currency,
            BigDecimal functionalAmount,
            String functionalCurrency,
            Instant createdAt
    ) {

        public static PostingResponse from(
                Posting posting
        ) {
            return new PostingResponse(
                    posting.id().value(),
                    posting.accountId().value(),
                    posting.type().name(),
                    posting.money().amount(),
                    posting.money().currency().code(),
                    posting.functionalMoney().amount(),
                    posting.functionalMoney().currency().code(),
                    posting.createdAt()
            );
        }
    }
}