package com.lopreti.ledger.flow.ledger.domain;

import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.shared.domain.Currency;
import com.lopreti.ledger.flow.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private static final Currency BRL =
            Currency.of("BRL");

    private static final AccountId SOURCE_ACCOUNT =
            AccountId.generate();

    private static final AccountId TARGET_ACCOUNT =
            AccountId.generate();

    @Test
    void shouldCreatePendingTransaction() {
        Transaction transaction =
                Transaction.create(
                        TransactionId.generate(),
                        Instant.now()
                );

        assertEquals(
                TransactionStatus.PENDING,
                transaction.status()
        );

        assertTrue(
                transaction.postings().isEmpty()
        );
    }

    @Test
    void shouldPostBalancedTransaction() {
        Transaction transaction =
                Transaction.create(
                        TransactionId.generate(),
                        Instant.now()
                );

        Money amount = Money.of(
                new BigDecimal("100.00"),
                BRL
        );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        SOURCE_ACCOUNT,
                        amount,
                        Instant.now()
                )
        );

        transaction.addPosting(
                Posting.credit(
                        PostingId.generate(),
                        TARGET_ACCOUNT,
                        amount,
                        Instant.now()
                )
        );

        transaction.post();

        assertEquals(
                TransactionStatus.POSTED,
                transaction.status()
        );
    }

    @Test
    void shouldRejectUnbalancedTransaction() {
        Transaction transaction =
                Transaction.create(
                        TransactionId.generate(),
                        Instant.now()
                );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        SOURCE_ACCOUNT,
                        Money.of(
                                new BigDecimal("100.00"),
                                BRL
                        ),
                        Instant.now()
                )
        );

        transaction.addPosting(
                Posting.credit(
                        PostingId.generate(),
                        TARGET_ACCOUNT,
                        Money.of(
                                new BigDecimal("90.00"),
                                BRL
                        ),
                        Instant.now()
                )
        );

        assertThrows(
                IllegalStateException.class,
                transaction::post
        );

        assertEquals(
                TransactionStatus.PENDING,
                transaction.status()
        );
    }

    @Test
    void shouldNotAllowPostingWithLessThanTwoPostings() {
        Transaction transaction =
                Transaction.create(
                        TransactionId.generate(),
                        Instant.now()
                );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        SOURCE_ACCOUNT,
                        Money.of(
                                new BigDecimal("100.00"),
                                BRL
                        ),
                        Instant.now()
                )
        );

        assertThrows(
                IllegalStateException.class,
                transaction::post
        );
    }

    @Test
    void shouldNotAllowModificationAfterPosting() {
        Transaction transaction =
                Transaction.create(
                        TransactionId.generate(),
                        Instant.now()
                );

        Money amount = Money.of(
                new BigDecimal("100.00"),
                BRL
        );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        SOURCE_ACCOUNT,
                        amount,
                        Instant.now()
                )
        );

        transaction.addPosting(
                Posting.credit(
                        PostingId.generate(),
                        TARGET_ACCOUNT,
                        amount,
                        Instant.now()
                )
        );

        transaction.post();

        assertThrows(
                IllegalStateException.class,
                () -> transaction.addPosting(
                        Posting.credit(
                                PostingId.generate(),
                                TARGET_ACCOUNT,
                                amount,
                                Instant.now()
                        )
                )
        );
    }

    @Test
    void shouldReversePostedTransaction() {
        Transaction transaction =
                Transaction.create(
                        TransactionId.generate(),
                        Instant.now()
                );

        Money amount = Money.of(
                new BigDecimal("100.00"),
                BRL
        );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        SOURCE_ACCOUNT,
                        amount,
                        Instant.now()
                )
        );

        transaction.addPosting(
                Posting.credit(
                        PostingId.generate(),
                        TARGET_ACCOUNT,
                        amount,
                        Instant.now()
                )
        );

        transaction.post();
        transaction.reverse();

        assertEquals(
                TransactionStatus.REVERSED,
                transaction.status()
        );
    }
}