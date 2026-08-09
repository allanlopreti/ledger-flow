package com.lopreti.ledger.flow.account.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerIdTest {

    @Test
    void shouldGenerateUuidV7() {
        CustomerId customerId = CustomerId.generate();

        assertNotNull(customerId);
        assertNotNull(customerId.value());

        assertEquals(7, customerId.value().version());
        assertEquals(2, customerId.value().variant());
    }

    @Test
    void shouldCreateFromExistingUuid() {
        UUID uuid = UUID.randomUUID();

        CustomerId customerId = CustomerId.of(uuid);

        assertEquals(uuid, customerId.value());
    }

    @Test
    void shouldRejectNullUuid() {
        assertThrows(
                NullPointerException.class,
                () -> new CustomerId(null)
        );
    }

    @Test
    void shouldGenerateDifferentIds() {
        CustomerId first = CustomerId.generate();
        CustomerId second = CustomerId.generate();

        assertNotEquals(first, second);
    }

    @Test
    void shouldGenerateTimeOrderedIds() {
        CustomerId first = CustomerId.generate();
        CustomerId second = CustomerId.generate();

        assertTrue(
                first.value().compareTo(second.value()) < 0
        );
    }
}