package com.lopreti.ledger.flow.account.infrastructure.persistence;

import com.lopreti.ledger.flow.account.domain.Account;
import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.account.domain.AccountStatus;
import com.lopreti.ledger.flow.account.domain.CustomerId;
import com.lopreti.ledger.flow.shared.domain.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class AccountRepositoryAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private AccountRepositoryAdapter accountRepository;

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );
    }

    @Test
    void shouldSaveAndRetrieveAccount() {

        var accountId = AccountId.generate();
        var customerId = CustomerId.of(UUID.randomUUID());
        var currency = Currency.of("BRL");

        var createdAt = Instant.now();

        var account = Account.create(
                accountId,
                customerId,
                currency,
                createdAt
        );

        accountRepository.save(account);

        var result = accountRepository.findById(accountId);

        assertTrue(result.isPresent());

        var persisted = result.get();

        assertEquals(
                accountId,
                persisted.id()
        );

        assertEquals(
                customerId,
                persisted.customerId()
        );

        assertEquals(
                currency,
                persisted.currency()
        );

        assertEquals(
                AccountStatus.ACTIVE,
                persisted.status()
        );

        assertInstantCloseTo(
                createdAt,
                persisted.createdAt()
        );
    }

    @Test
    void shouldReturnEmptyWhenAccountDoesNotExist() {

        var result = accountRepository.findById(
                AccountId.generate()
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCheckIfAccountExists() {

        var accountId = AccountId.generate();
        var customerId = CustomerId.of(UUID.randomUUID());
        var currency = Currency.of("BRL");

        var account = Account.create(
                accountId,
                customerId,
                currency,
                Instant.now()
        );

        accountRepository.save(account);

        assertTrue(
                accountRepository.existsById(accountId)
        );
    }

    @Test
    void shouldReturnFalseWhenAccountDoesNotExist() {

        assertFalse(
                accountRepository.existsById(
                        AccountId.generate()
                )
        );
    }

    private void assertInstantCloseTo(
            Instant expected,
            Instant actual
    ) {
        var difference = Math.abs(
                Duration.between(
                        expected,
                        actual
                ).toNanos()
        );

        assertTrue(
                difference <= 1_000,
                () -> String.format(
                        "Expected %s but was %s. Difference: %d ns",
                        expected,
                        actual,
                        difference
                )
        );
    }
}
