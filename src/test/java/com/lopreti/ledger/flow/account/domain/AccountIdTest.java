package com.lopreti.ledger.flow.account.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountIdTest {

    @Test
    void shouldGenerateUuidV7() {
        AccountId accountId = AccountId.generate();

        assertNotNull(accountId);
        assertNotNull(accountId.value());

        assertEquals(7, accountId.value().version());
        assertEquals(2, accountId.value().variant());
    }

    @Test
    void shouldCreateFromExistingUuid() {
        UUID uuid = UUID.randomUUID();

        AccountId accountId = AccountId.of(uuid);

        assertEquals(uuid, accountId.value());
    }

    @Test
    void shouldRejectNullUuid() {
        assertThrows(
                NullPointerException.class,
                () -> new AccountId(null)
        );
    }

    @Test
    void shouldGenerateDifferentIds() {
        AccountId first = AccountId.generate();
        AccountId second = AccountId.generate();

        assertNotEquals(first, second);
    }
}