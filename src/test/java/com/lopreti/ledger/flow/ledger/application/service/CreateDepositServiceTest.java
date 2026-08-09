package com.lopreti.ledger.flow.ledger.application.service;

import com.lopreti.ledger.flow.account.application.port.out.AccountRepository;
import com.lopreti.ledger.flow.account.domain.Account;
import com.lopreti.ledger.flow.account.domain.AccountBalance;
import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.account.domain.CustomerId;
import com.lopreti.ledger.flow.shared.domain.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateDepositServiceTest {

    private static final Currency BRL =
            Currency.of("BRL");

    private static final CustomerId CUSTOMER_ID =
            CustomerId.of(UUID.randomUUID());

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-08T12:00:00Z");

    private AccountRepository accountRepository;

    private CreateDepositService service;

    @BeforeEach
    void setUp() {
        accountRepository =
                mock(AccountRepository.class);

        service =
                new CreateDepositService(
                        accountRepository
                );
    }

    @Test
    void shouldDepositMoneyIntoAccount() {

        AccountId accountId =
                AccountId.generate();

        Account account =
                Account.create(
                        accountId,
                        CUSTOMER_ID,
                        BRL,
                        CREATED_AT
                );

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        AccountBalance balance =
                service.execute(
                        accountId,
                        new BigDecimal("500.00"),
                        BRL
                );

        assertEquals(
                0,
                new BigDecimal("500.00").compareTo(balance.amount())
        );

        assertEquals(
                BRL,
                balance.currency()
        );

        verify(accountRepository)
                .save(account);
    }

    @Test
    void shouldAccumulateDeposits() {

        AccountId accountId =
                AccountId.generate();

        Account account =
                Account.create(
                        accountId,
                        CUSTOMER_ID,
                        BRL,
                        CREATED_AT
                );

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        service.execute(
                accountId,
                new BigDecimal("500.00"),
                BRL
        );

        AccountBalance balance =
                service.execute(
                        accountId,
                        new BigDecimal("250.00"),
                        BRL
                );

        assertEquals(
                0,
                new BigDecimal("750.00").compareTo(balance.amount())
        );
    }

    @Test
    void shouldRejectDepositWhenAccountDoesNotExist() {

        AccountId accountId =
                AccountId.generate();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.execute(
                                accountId,
                                new BigDecimal("100.00"),
                                BRL
                        )
                );

        assertEquals(
                "Account not found: " + accountId.value(),
                exception.getMessage()
        );

        verify(accountRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectDepositWithDifferentCurrency() {

        AccountId accountId =
                AccountId.generate();

        Account account =
                Account.create(
                        accountId,
                        CUSTOMER_ID,
                        BRL,
                        CREATED_AT
                );

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        Currency USD =
                Currency.of("USD");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.execute(
                                accountId,
                                new BigDecimal("100.00"),
                                USD
                        )
                );

        assertEquals(
                "Currency mismatch",
                exception.getMessage()
        );

        verify(accountRepository, never())
                .save(any());
    }
}
