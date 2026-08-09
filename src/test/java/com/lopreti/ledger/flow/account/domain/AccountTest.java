package com.lopreti.ledger.flow.account.domain;

import com.lopreti.ledger.flow.shared.domain.Currency;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private static final Currency BRL =
            Currency.of("BRL");

    private static final CustomerId CUSTOMER_ID =
            CustomerId.of(UUID.randomUUID());

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-08T12:00:00Z");

    @Test
    void shouldCreateAccount() {
        AccountId accountId =
                AccountId.generate();

        Account account = Account.create(
                accountId,
                CUSTOMER_ID,
                BRL,
                CREATED_AT
        );

        assertEquals(
                accountId,
                account.id()
        );

        assertEquals(
                CUSTOMER_ID,
                account.customerId()
        );

        assertEquals(
                BRL,
                account.currency()
        );

        assertEquals(
                AccountStatus.ACTIVE,
                account.status()
        );

        assertEquals(
                CREATED_AT,
                account.createdAt()
        );
    }

    @Test
    void shouldCreateAccountWithActiveStatus() {
        Account account = createAccount();

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
    void shouldCloseBlockedAccount() {
        Account account = createAccount();

        account.block();
        account.close();

        assertEquals(
                AccountStatus.CLOSED,
                account.status()
        );
    }

    @Test
    void shouldRejectBlockingClosedAccount() {
        Account account = createAccount();

        account.close();

        assertThrows(
                IllegalStateException.class,
                account::block
        );
    }

    @Test
    void shouldRejectActivatingClosedAccount() {
        Account account = createAccount();

        account.close();

        assertThrows(
                IllegalStateException.class,
                account::activate
        );
    }

    @Test
    void shouldRejectClosingAlreadyClosedAccount() {
        Account account = createAccount();

        account.close();

        assertThrows(
                IllegalStateException.class,
                account::close
        );
    }

    @Test
    void shouldAllowActivatingAlreadyActiveAccount() {
        Account account = createAccount();

        account.activate();

        assertEquals(
                AccountStatus.ACTIVE,
                account.status()
        );
    }

    private Account createAccount() {
        return Account.create(
                AccountId.generate(),
                CUSTOMER_ID,
                BRL,
                CREATED_AT
        );
    }
}