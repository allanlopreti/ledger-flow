package com.lopreti.ledger.flow.account.domain;

import com.lopreti.ledger.flow.shared.domain.Currency;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void shouldCreateActiveAccount() {
        AccountId accountId = AccountId.generate();
        CustomerId customerId = CustomerId.generate();
        Currency currency = Currency.of("BRL");
        Instant createdAt = Instant.now();

        Account account = Account.create(
                accountId,
                customerId,
                currency,
                createdAt
        );

        assertEquals(accountId, account.id());
        assertEquals(customerId, account.customerId());
        assertEquals(currency, account.currency());
        assertEquals(createdAt, account.createdAt());

        assertEquals(
                AccountStatus.ACTIVE,
                account.status()
        );
    }

    @Test
    void shouldBlockActiveAccount() {
        Account account = createAccount();

        account.block();

        assertEquals(
                AccountStatus.BLOCKED,
                account.status()
        );
    }

    @Test
    void shouldActivateBlockedAccount() {
        Account account = createAccount();

        account.block();
        account.activate();

        assertEquals(
                AccountStatus.ACTIVE,
                account.status()
        );
    }

    @Test
    void shouldCloseActiveAccount() {
        Account account = createAccount();

        account.close();

        assertEquals(
                AccountStatus.CLOSED,
                account.status()
        );
    }

    @Test
    void shouldNotBlockClosedAccount() {
        Account account = createAccount();

        account.close();

        assertThrows(
                IllegalStateException.class,
                account::block
        );
    }

    @Test
    void shouldNotActivateClosedAccount() {
        Account account = createAccount();

        account.close();

        assertThrows(
                IllegalStateException.class,
                account::activate
        );
    }

    @Test
    void shouldNotCloseAlreadyClosedAccount() {
        Account account = createAccount();

        account.close();

        assertThrows(
                IllegalStateException.class,
                account::close
        );
    }

    @Test
    void shouldGenerateTimeOrderedIds() {
        AccountId first = AccountId.generate();
        AccountId second = AccountId.generate();

        assertTrue(
                first.value().compareTo(second.value()) < 0
        );
    }

    private Account createAccount() {
        return Account.create(
                AccountId.generate(),
                CustomerId.generate(),
                Currency.of("BRL"),
                Instant.now()
        );
    }
}