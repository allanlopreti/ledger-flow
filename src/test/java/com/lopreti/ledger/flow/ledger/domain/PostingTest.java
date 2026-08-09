package com.lopreti.ledger.flow.ledger.domain;

import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.shared.domain.Currency;
import com.lopreti.ledger.flow.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PostingTest {

    private static final Currency BRL =
            Currency.of("BRL");

    private static final AccountId ACCOUNT_ID =
            AccountId.generate();

    @Test
    void shouldCreateDebitPosting() {

        Money money = Money.of(
                new BigDecimal("100.00"),
                BRL
        );

        Money baseMoney = Money.of(
                new BigDecimal("100.00"),
                BRL
        );

        Posting posting = Posting.debit(
                PostingId.generate(),
                ACCOUNT_ID,
                money,
                baseMoney,
                Instant.now()
        );

        assertEquals(
                PostingType.DEBIT,
                posting.type()
        );

        assertEquals(
                money,
                posting.money()
        );

        assertEquals(
                baseMoney,
                posting.baseMoney()
        );

        assertEquals(
                ACCOUNT_ID,
                posting.accountId()
        );
    }

    @Test
    void shouldCreateCreditPosting() {

        Money money = Money.of(
                new BigDecimal("100.00"),
                BRL
        );

        Money baseMoney = Money.of(
                new BigDecimal("100.00"),
                BRL
        );

        Posting posting = Posting.credit(
                PostingId.generate(),
                ACCOUNT_ID,
                money,
                baseMoney,
                Instant.now()
        );

        assertEquals(
                PostingType.CREDIT,
                posting.type()
        );

        assertEquals(
                money,
                posting.money()
        );

        assertEquals(
                baseMoney,
                posting.baseMoney()
        );
    }

    @Test
    void shouldRejectZeroAmount() {

        Money money = Money.zero(BRL);

        Money baseMoney = Money.zero(BRL);

        assertThrows(
                IllegalArgumentException.class,
                () -> Posting.debit(
                        PostingId.generate(),
                        ACCOUNT_ID,
                        money,
                        baseMoney,
                        Instant.now()
                )
        );
    }

    @Test
    void shouldRejectNegativeAmount() {

        Money money = Money.of(
                new BigDecimal("-100.00"),
                BRL
        );

        Money baseMoney = Money.of(
                new BigDecimal("-100.00"),
                BRL
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> Posting.credit(
                        PostingId.generate(),
                        ACCOUNT_ID,
                        money,
                        baseMoney,
                        Instant.now()
                )
        );
    }
}
