package com.lopreti.ledger.flow.ledger.application.service;

import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.ledger.application.port.out.TransactionRepository;
import com.lopreti.ledger.flow.ledger.domain.Posting;
import com.lopreti.ledger.flow.ledger.domain.PostingId;
import com.lopreti.ledger.flow.ledger.domain.Transaction;
import com.lopreti.ledger.flow.ledger.domain.TransactionId;
import com.lopreti.ledger.flow.ledger.domain.TransactionStatus;
import com.lopreti.ledger.flow.shared.domain.Currency;
import com.lopreti.ledger.flow.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTransactionHistoryServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private GetTransactionHistoryService service;

    @Test
    void shouldReturnAllTransactions() {

        Transaction transaction1 = transaction(
                "019fe4c6-4eb2-7e8c-82dc-7bb3b52b7f5a",
                "019fe4c6-a774-7837-b1da-5d5043ea9d52"
        );

        Transaction transaction2 = transaction(
                "019fe497-eb37-76db-8242-9ebb16c400e0",
                "019fe48c-4641-7f8c-85da-c90192fa9dfb"
        );

        when(transactionRepository.findAll())
                .thenReturn(List.of(transaction1, transaction2));

        var result = service.execute();

        assertEquals(2, result.size());
        assertEquals(transaction1.id(), result.get(0).id());
        assertEquals(transaction2.id(), result.get(1).id());
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoTransactions() {

        when(transactionRepository.findAll())
                .thenReturn(List.of());

        var result = service.execute();

        assertEquals(0, result.size());
    }

    private Transaction transaction(
            String transactionId,
            String accountId
    ) {

        Instant now = Instant.now();

        Posting posting = Posting.debit(
                PostingId.generate(),
                AccountId.of(UUID.fromString(accountId)),
                Money.of(
                        new BigDecimal("100.00"),
                        Currency.of("BRL")
                ),
                Money.of(
                        new BigDecimal("100.00"),
                        Currency.of("BRL")
                ),
                now
        );

        return Transaction.reconstitute(
                TransactionId.of(
                        UUID.fromString(transactionId)
                ),
                Currency.of("BRL"),
                now,
                TransactionStatus.POSTED,
                List.of(posting)
        );
    }

}
