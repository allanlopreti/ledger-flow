package com.lopreti.ledger.flow.account.application.service;

import com.lopreti.ledger.flow.account.application.port.out.AccountRepository;
import com.lopreti.ledger.flow.account.domain.Account;
import com.lopreti.ledger.flow.account.domain.AccountStatus;
import com.lopreti.ledger.flow.account.domain.CustomerId;
import com.lopreti.ledger.flow.shared.domain.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private Clock clock;

    private CreateAccountService service;

    private final Instant fixedInstant =
            Instant.parse("2026-08-09T03:00:00Z");

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                fixedInstant,
                ZoneOffset.UTC
        );

        service = new CreateAccountService(
                accountRepository,
                clock
        );
    }

    @Test
    void shouldCreateAccount() {

        var customerId = CustomerId.of(
                UUID.randomUUID()
        );

        var currency = Currency.of("BRL");

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        var result = service.execute(
                customerId,
                currency
        );

        assertNotNull(result);

        assertNotNull(result.id());

        assertEquals(
                customerId,
                result.customerId()
        );

        assertEquals(
                currency,
                result.currency()
        );

        assertEquals(
                AccountStatus.ACTIVE,
                result.status()
        );

        assertEquals(
                fixedInstant,
                result.createdAt()
        );

        verify(accountRepository, times(1))
                .save(any(Account.class));
    }

    @Test
    void shouldGenerateDifferentAccountIds() {

        var customerId = CustomerId.of(
                UUID.randomUUID()
        );

        var currency = Currency.of("BRL");

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        var first = service.execute(
                customerId,
                currency
        );

        var second = service.execute(
                customerId,
                currency
        );

        assertNotEquals(
                first.id(),
                second.id()
        );

        verify(accountRepository, times(2))
                .save(any(Account.class));
    }

    @Test
    void shouldSaveCreatedAccount() {

        var customerId = CustomerId.of(
                UUID.randomUUID()
        );

        var currency = Currency.of("BRL");

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        service.execute(
                customerId,
                currency
        );

        ArgumentCaptor<Account> captor =
                ArgumentCaptor.forClass(Account.class);

        verify(accountRepository)
                .save(captor.capture());

        var savedAccount = captor.getValue();

        assertNotNull(savedAccount);

        assertNotNull(savedAccount.id());

        assertEquals(
                customerId,
                savedAccount.customerId()
        );

        assertEquals(
                currency,
                savedAccount.currency()
        );

        assertEquals(
                AccountStatus.ACTIVE,
                savedAccount.status()
        );

        assertEquals(
                fixedInstant,
                savedAccount.createdAt()
        );
    }
}
