package com.lopreti.ledger.flow.ledger.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionIdTest {

    @Test
    void shouldGenerateUuidV7() {
        TransactionId id =
                TransactionId.generate();

        assertNotNull(id.value());

        assertEquals(
                7,
                id.value().version()
        );

        assertEquals(
                2,
                id.value().variant()
        );
    }

    @Test
    void shouldCreateFromExistingUuid() {
        UUID uuid = UUID.randomUUID();

        TransactionId id =
                TransactionId.of(uuid);

        assertEquals(
                uuid,
                id.value()
        );
    }

    @Test
    void shouldRejectNullUuid() {
        assertThrows(
                NullPointerException.class,
                () -> new TransactionId(null)
        );
    }
}