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
    void shouldPostBalancedMultiCurrencyTransaction() {

        var usd = Currency.of("USD");
        var brl = Currency.of("BRL");

        var transaction = Transaction.create(
                TransactionId.generate(),
                brl,
                Instant.now()
        );

        var sourceAccount = AccountId.generate();
        var targetAccount = AccountId.generate();

        var sourceMoney = Money.of(
                new BigDecimal("100.00"),
                usd
        );

        var targetMoney = Money.of(
                new BigDecimal("520.00"),
                brl
        );

        var sourceBaseMoney = Money.of(
                new BigDecimal("520.00"),
                brl
        );

        var targetBaseMoney = Money.of(
                new BigDecimal("520.00"),
                brl
        );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        sourceAccount,
                        sourceMoney,
                        sourceBaseMoney,
                        Instant.now()
                )
        );

        transaction.addPosting(
                Posting.credit(
                        PostingId.generate(),
                        targetAccount,
                        targetMoney,
                        targetBaseMoney,
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
    void shouldRejectUnbalancedMultiCurrencyTransaction() {

        var usd = Currency.of("USD");
        var brl = Currency.of("BRL");

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
                                new BigDecimal("500.00"),
                                brl
                        ),
                        Money.of(
                                new BigDecimal("500.00"),
                                brl
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
    void shouldRejectPostingWithWrongBaseCurrency() {

        var brl = Currency.of("BRL");
        var usd = Currency.of("USD");

        var transaction = Transaction.create(
                TransactionId.generate(),
                brl,
                Instant.now()
        );

        var money = Money.of(
                new BigDecimal("100.00"),
                usd
        );

        var wrongBaseMoney = Money.of(
                new BigDecimal("520.00"),
                usd
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> transaction.addPosting(
                        Posting.debit(
                                PostingId.generate(),
                                AccountId.generate(),
                                money,
                                wrongBaseMoney,
                                Instant.now()
                        )
                )
        );
    }
}