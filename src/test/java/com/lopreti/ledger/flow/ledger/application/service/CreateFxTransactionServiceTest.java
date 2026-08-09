package com.lopreti.ledger.flow.ledger.application.service;

import com.lopreti.ledger.flow.account.application.port.out.AccountRepository;
import com.lopreti.ledger.flow.account.domain.Account;
import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.account.domain.CustomerId;
import com.lopreti.ledger.flow.ledger.application.port.in.CreateTransactionUseCase;
import com.lopreti.ledger.flow.ledger.domain.Transaction;
import com.lopreti.ledger.flow.shared.domain.Currency;
import com.lopreti.ledger.flow.shared.domain.ExchangeRate;
import com.lopreti.ledger.flow.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateFxTransactionServiceTest {

    @Mock
    private CreateTransactionUseCase createTransactionUseCase;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CreateFxTransactionService service;

    private AccountId debitAccountId;
    private AccountId creditAccountId;

    private Account debitAccount;
    private Account creditAccount;

    private Money debitMoney;
    private ExchangeRate usdToBrl;

    @BeforeEach
    void setUp() {

        debitAccountId = AccountId.generate();
        creditAccountId = AccountId.generate();

        debitAccount = Account.create(
                debitAccountId,
                CustomerId.generate(),
                Currency.of("USD"),
                Instant.now()
        );

        creditAccount = Account.create(
                creditAccountId,
                CustomerId.generate(),
                Currency.of("BRL"),
                Instant.now()
        );

        debitMoney = Money.of(
                new BigDecimal("100.00"),
                Currency.of("USD")
        );

        usdToBrl = new ExchangeRate(
                Currency.of("USD"),
                Currency.of("BRL"),
                new BigDecimal("5.40")
        );
    }

    @Test
    void shouldCreateUsdToBrlTransaction() {

        Transaction transaction = Transaction.create(
                com.lopreti.ledger.flow.ledger.domain.TransactionId.generate(),
                Currency.of("BRL"),
                Instant.now()
        );

        when(accountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(debitAccount));

        when(accountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(creditAccount));

        when(createTransactionUseCase.execute(
                eq(debitAccountId),
                any(Money.class),
                any(Money.class),
                eq(creditAccountId),
                any(Money.class),
                any(Money.class)
        )).thenReturn(transaction);

        Transaction result = service.execute(
                debitAccountId,
                debitMoney,
                creditAccountId,
                usdToBrl
        );

        assertEquals(
                transaction.id(),
                result.id()
        );

        ArgumentCaptor<Money> moneyCaptor =
                ArgumentCaptor.forClass(Money.class);

        verify(createTransactionUseCase)
                .execute(
                        eq(debitAccountId),
                        eq(debitMoney),
                        moneyCaptor.capture(),
                        eq(creditAccountId),
                        moneyCaptor.capture(),
                        moneyCaptor.capture()
                );

        var captured = moneyCaptor.getAllValues();

        // Debit functional amount
        assertEquals(
                new BigDecimal("540.00"),
                captured.get(0).amount()
        );

        assertEquals(
                Currency.of("BRL"),
                captured.get(0).currency()
        );

        // Credit amount
        assertEquals(
                new BigDecimal("540.00"),
                captured.get(1).amount()
        );

        assertEquals(
                Currency.of("BRL"),
                captured.get(1).currency()
        );

        // Credit functional amount
        assertEquals(
                new BigDecimal("540.00"),
                captured.get(2).amount()
        );

        assertEquals(
                Currency.of("BRL"),
                captured.get(2).currency()
        );
    }

    @Test
    void shouldFailWhenDebitAccountDoesNotExist() {

        when(accountRepository.findById(debitAccountId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.execute(
                                debitAccountId,
                                debitMoney,
                                creditAccountId,
                                usdToBrl
                        )
                );

        assertEquals(
                "Debit account not found",
                exception.getMessage()
        );

        verify(createTransactionUseCase, never())
                .execute(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldFailWhenCreditAccountDoesNotExist() {

        when(accountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(debitAccount));

        when(accountRepository.findById(creditAccountId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.execute(
                                debitAccountId,
                                debitMoney,
                                creditAccountId,
                                usdToBrl
                        )
                );

        assertEquals(
                "Credit account not found",
                exception.getMessage()
        );

        verify(createTransactionUseCase, never())
                .execute(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldFailWhenDebitAccountCurrencyDoesNotMatchExchangeRateSource() {

        Account eurDebitAccount = Account.create(
                debitAccountId,
                CustomerId.generate(),
                Currency.of("EUR"),
                Instant.now()
        );

        when(accountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(eurDebitAccount));

        when(accountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(creditAccount));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.execute(
                                debitAccountId,
                                debitMoney,
                                creditAccountId,
                                usdToBrl
                        )
                );

        assertEquals(
                "Debit account currency does not match debit money currency",
                exception.getMessage()
        );

        verify(createTransactionUseCase, never())
                .execute(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldFailWhenCreditAccountCurrencyDoesNotMatchExchangeRateTarget() {

        Account eurCreditAccount = Account.create(
                creditAccountId,
                CustomerId.generate(),
                Currency.of("EUR"),
                Instant.now()
        );

        when(accountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(debitAccount));

        when(accountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(eurCreditAccount));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.execute(
                                debitAccountId,
                                debitMoney,
                                creditAccountId,
                                usdToBrl
                        )
                );

        assertEquals(
                "Credit account currency does not match exchange rate target currency",
                exception.getMessage()
        );

        verify(createTransactionUseCase, never())
                .execute(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldFailWhenExchangeRateSourceDoesNotMatchDebitAccount() {

        ExchangeRate eurToBrl = new ExchangeRate(
                Currency.of("EUR"),
                Currency.of("BRL"),
                new BigDecimal("6.00")
        );

        when(accountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(debitAccount));

        when(accountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(creditAccount));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.execute(
                                debitAccountId,
                                debitMoney,
                                creditAccountId,
                                eurToBrl
                        )
                );

        assertEquals(
                "Debit account currency does not match exchange rate source currency",
                exception.getMessage()
        );

        verify(createTransactionUseCase, never())
                .execute(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldFailWhenExchangeRateTargetDoesNotMatchCreditAccount() {

        ExchangeRate usdToEur = new ExchangeRate(
                Currency.of("USD"),
                Currency.of("EUR"),
                new BigDecimal("0.92")
        );

        when(accountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(debitAccount));

        when(accountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(creditAccount));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.execute(
                                debitAccountId,
                                debitMoney,
                                creditAccountId,
                                usdToEur
                        )
                );

        assertEquals(
                "Credit account currency does not match exchange rate target currency",
                exception.getMessage()
        );

        verify(createTransactionUseCase, never())
                .execute(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldConvertUsingExchangeRate() {

        when(accountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(debitAccount));

        when(accountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(creditAccount));

        Transaction transaction = Transaction.create(
                com.lopreti.ledger.flow.ledger.domain.TransactionId.generate(),
                Currency.of("BRL"),
                Instant.now()
        );

        when(createTransactionUseCase.execute(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(transaction);

        service.execute(
                debitAccountId,
                Money.of(
                        new BigDecimal("250.00"),
                        Currency.of("USD")
                ),
                creditAccountId,
                usdToBrl
        );

        ArgumentCaptor<Money> moneyCaptor =
                ArgumentCaptor.forClass(Money.class);

        verify(createTransactionUseCase)
                .execute(
                        eq(debitAccountId),
                        eq(Money.of(
                                new BigDecimal("250.00"),
                                Currency.of("USD")
                        )),
                        moneyCaptor.capture(),
                        eq(creditAccountId),
                        moneyCaptor.capture(),
                        moneyCaptor.capture()
                );

        var captured = moneyCaptor.getAllValues();

        assertEquals(
                new BigDecimal("1350.00"),
                captured.get(0).amount()
        );

        assertEquals(
                new BigDecimal("1350.00"),
                captured.get(1).amount()
        );

        assertEquals(
                new BigDecimal("1350.00"),
                captured.get(2).amount()
        );

        assertEquals(
                Currency.of("BRL"),
                captured.get(0).currency()
        );
    }

    @Test
    void shouldFailWhenDebitAccountHasInsufficientBalance() {

        when(accountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(debitAccount));

        when(accountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(creditAccount));

        when(createTransactionUseCase.execute(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        )).thenThrow(
                new IllegalStateException(
                        "Insufficient account balance"
                )
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.execute(
                                debitAccountId,
                                debitMoney,
                                creditAccountId,
                                usdToBrl
                        )
                );

        assertEquals(
                "Insufficient account balance",
                exception.getMessage()
        );
    }
}
