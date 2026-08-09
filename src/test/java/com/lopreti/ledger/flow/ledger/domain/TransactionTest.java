package com.lopreti.ledger.flow.ledger.domain;

import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.shared.domain.Currency;
import com.lopreti.ledger.flow.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void shouldPostBalancedSameCurrencyTransaction() {
        var brl = Currency.of("BRL");

        var transaction = Transaction.create(
                TransactionId.generate(),
                brl,
                Instant.now()
        );

        var money = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        AccountId.generate(),
                        money,
                        money,
                        Instant.now()
                )
        );

        transaction.addPosting(
                Posting.credit(
                        PostingId.generate(),
                        AccountId.generate(),
                        money,
                        money,
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
    void shouldRejectTransactionWithOnlyOnePosting() {
        var brl = Currency.of("BRL");

        var transaction = Transaction.create(
                TransactionId.generate(),
                brl,
                Instant.now()
        );

        var money = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        AccountId.generate(),
                        money,
                        money,
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
    void shouldRejectPostingWithDuplicateId() {
        var brl = Currency.of("BRL");

        var transaction = Transaction.create(
                TransactionId.generate(),
                brl,
                Instant.now()
        );

        var postingId = PostingId.generate();

        var money = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var firstPosting = Posting.debit(
                postingId,
                AccountId.generate(),
                money,
                money,
                Instant.now()
        );

        var secondPosting = Posting.credit(
                postingId,
                AccountId.generate(),
                money,
                money,
                Instant.now()
        );

        transaction.addPosting(firstPosting);

        assertThrows(
                IllegalArgumentException.class,
                () -> transaction.addPosting(secondPosting)
        );
    }

    @Test
    void shouldNotAllowAddingPostingAfterTransactionIsPosted() {
        var brl = Currency.of("BRL");

        var transaction = Transaction.create(
                TransactionId.generate(),
                brl,
                Instant.now()
        );

        var money = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        AccountId.generate(),
                        money,
                        money,
                        Instant.now()
                )
        );

        transaction.addPosting(
                Posting.credit(
                        PostingId.generate(),
                        AccountId.generate(),
                        money,
                        money,
                        Instant.now()
                )
        );

        transaction.post();

        assertThrows(
                IllegalStateException.class,
                () -> transaction.addPosting(
                        Posting.debit(
                                PostingId.generate(),
                                AccountId.generate(),
                                money,
                                money,
                                Instant.now()
                        )
                )
        );
    }

    @Test
    void shouldRejectUnbalancedBaseMoney() {
        var brl = Currency.of("BRL");
        var usd = Currency.of("USD");

        var transaction = Transaction.create(
                TransactionId.generate(),
                brl,
                Instant.now()
        );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        AccountId.generate(),
                        Money.of(
                                new BigDecimal("100.00"),
                                usd
                        ),
                        Money.of(
                                new BigDecimal("520.00"),
                                brl
                        ),
                        Instant.now()
                )
        );

        transaction.addPosting(
                Posting.credit(
                        PostingId.generate(),
                        AccountId.generate(),
                        Money.of(
                                new BigDecimal("520.00"),
                                brl
                        ),
                        Money.of(
                                new BigDecimal("519.99"),
                                brl
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
    void shouldRejectReversingTransactionThatIsNotPosted() {
        var brl = Currency.of("BRL");

        var transaction = Transaction.create(
                TransactionId.generate(),
                brl,
                Instant.now()
        );

        assertThrows(
                IllegalStateException.class,
                transaction::reverse
        );
    }

    @Test
    void shouldReversePostedTransaction() {
        var brl = Currency.of("BRL");

        var transaction = Transaction.create(
                TransactionId.generate(),
                brl,
                Instant.now()
        );

        var money = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        AccountId.generate(),
                        money,
                        money,
                        Instant.now()
                )
        );

        transaction.addPosting(
                Posting.credit(
                        PostingId.generate(),
                        AccountId.generate(),
                        money,
                        money,
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

    @Test
    void shouldRejectReversingAlreadyReversedTransaction() {
        var brl = Currency.of("BRL");

        var transaction = Transaction.create(
                TransactionId.generate(),
                brl,
                Instant.now()
        );

        var money = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        AccountId.generate(),
                        money,
                        money,
                        Instant.now()
                )
        );

        transaction.addPosting(
                Posting.credit(
                        PostingId.generate(),
                        AccountId.generate(),
                        money,
                        money,
                        Instant.now()
                )
        );

        transaction.post();
        transaction.reverse();

        assertThrows(
                IllegalStateException.class,
                transaction::reverse
        );
    }
}