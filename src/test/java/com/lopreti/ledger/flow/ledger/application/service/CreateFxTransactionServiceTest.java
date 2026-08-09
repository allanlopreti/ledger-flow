package com.lopreti.ledger.flow.ledger.application.service;

import com.lopreti.ledger.flow.account.application.port.out.AccountRepository;
import com.lopreti.ledger.flow.account.domain.Account;
import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.account.domain.CustomerId;
import com.lopreti.ledger.flow.ledger.application.port.out.TransactionRepository;
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
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateFxTransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private CreateFxTransactionService service;

    private AccountId debitAccountId;
    private AccountId creditAccountId;

    private Account debitAccount;
    private Account creditAccount;

    private Money debitMoney;
    private ExchangeRate usdToBrl;

    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.parse("2026-08-09T02:00:00Z");

        lenient()
                .when(clock.instant())
                .thenReturn(now);

        debitAccountId = AccountId.generate();
        creditAccountId = AccountId.generate();

        debitAccount = Account.create(
                debitAccountId,
                CustomerId.generate(),
                Currency.of("USD"),
                now
        );

        creditAccount = Account.create(
                creditAccountId,
                CustomerId.generate(),
                Currency.of("BRL"),
                now
        );

        debitAccount.credit(
                Money.of(
                        new BigDecimal("1000.00"),
                        Currency.of("USD")
                )
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

        when(accountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(debitAccount));

        when(accountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(creditAccount));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.execute(
                debitAccountId,
                debitMoney,
                creditAccountId,
                usdToBrl
        );

        assertEquals(
                Currency.of("BRL"),
                result.baseCurrency()
        );

        assertEquals(
                "POSTED",
                result.status().name()
        );

        assertEquals(
                2,
                result.postings().size()
        );

        assertEquals(
                usdToBrl,
                result.exchangeRate()
        );

        assertEquals(
                now,
                result.createdAt()
        );

        verify(transactionRepository)
                .save(any(Transaction.class));

        assertEquals(
                0,
                debitAccount.balance()
                        .amount()
                        .compareTo(new BigDecimal("900.00"))
        );

        assertEquals(
                0,
                creditAccount.balance()
                        .amount()
                        .compareTo(new BigDecimal("540.00"))
        );
    }

    @Test
    void shouldPersistExchangeRateInTransaction() {

        when(accountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(debitAccount));

        when(accountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(creditAccount));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.execute(
                debitAccountId,
                debitMoney,
                creditAccountId,
                usdToBrl
        );

        assertEquals(
                usdToBrl,
                result.exchangeRate()
        );

        assertEquals(
                Currency.of("USD"),
                result.exchangeRate().from()
        );

        assertEquals(
                Currency.of("BRL"),
                result.exchangeRate().to()
        );

        assertEquals(
                0,
                result.exchangeRate()
                        .rate()
                        .compareTo(new BigDecimal("5.40"))
        );

        assertEquals(
                now,
                result.createdAt()
        );
    }

    @Test
    void shouldPersistExchangeRateWhenSavingTransaction() {

        when(accountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(debitAccount));

        when(accountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(creditAccount));

        ArgumentCaptor<Transaction> transactionCaptor =
                ArgumentCaptor.forClass(Transaction.class);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.execute(
                debitAccountId,
                debitMoney,
                creditAccountId,
                usdToBrl
        );

        verify(transactionRepository)
                .save(transactionCaptor.capture());

        Transaction savedTransaction =
                transactionCaptor.getValue();

        assertEquals(
                Currency.of("USD"),
                savedTransaction.exchangeRate().from()
        );

        assertEquals(
                Currency.of("BRL"),
                savedTransaction.exchangeRate().to()
        );

        assertEquals(
                0,
                savedTransaction.exchangeRate()
                        .rate()
                        .compareTo(new BigDecimal("5.40"))
        );

        assertEquals(
                now,
                savedTransaction.createdAt()
        );
    }

    @Test
    void shouldConvertUsingExchangeRate() {

        when(accountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(debitAccount));

        when(accountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(creditAccount));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.execute(
                debitAccountId,
                Money.of(
                        new BigDecimal("250.00"),
                        Currency.of("USD")
                ),
                creditAccountId,
                usdToBrl
        );

        var debitPosting =
                result.postings()
                        .stream()
                        .filter(posting ->
                                posting.type()
                                        .name()
                                        .equals("DEBIT"))
                        .findFirst()
                        .orElseThrow();

        var creditPosting =
                result.postings()
                        .stream()
                        .filter(posting ->
                                posting.type()
                                        .name()
                                        .equals("CREDIT"))
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                0,
                debitPosting.money()
                        .amount()
                        .compareTo(new BigDecimal("250.00"))
        );

        assertEquals(
                Currency.of("USD"),
                debitPosting.money().currency()
        );

        assertEquals(
                0,
                debitPosting.functionalMoney()
                        .amount()
                        .compareTo(new BigDecimal("1350.00"))
        );

        assertEquals(
                Currency.of("BRL"),
                debitPosting.functionalMoney().currency()
        );

        assertEquals(
                0,
                creditPosting.money()
                        .amount()
                        .compareTo(new BigDecimal("1350.00"))
        );

        assertEquals(
                Currency.of("BRL"),
                creditPosting.money().currency()
        );

        assertEquals(
                0,
                creditPosting.functionalMoney()
                        .amount()
                        .compareTo(new BigDecimal("1350.00"))
        );

        assertEquals(
                now,
                result.createdAt()
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

        verify(transactionRepository, never())
                .save(any(Transaction.class));
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

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldFailWhenDebitAccountCurrencyDoesNotMatchDebitMoney() {

        Account eurDebitAccount =
                Account.create(
                        debitAccountId,
                        CustomerId.generate(),
                        Currency.of("EUR"),
                        now
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

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldFailWhenExchangeRateSourceDoesNotMatchDebitAccount() {

        ExchangeRate eurToBrl =
                new ExchangeRate(
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

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldFailWhenCreditAccountCurrencyDoesNotMatchExchangeRateTarget() {

        Account eurCreditAccount =
                Account.create(
                        creditAccountId,
                        CustomerId.generate(),
                        Currency.of("EUR"),
                        now
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

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldFailWhenExchangeRateTargetDoesNotMatchCreditAccount() {

        ExchangeRate usdToEur =
                new ExchangeRate(
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

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldFailWhenDebitAccountHasInsufficientBalance() {

        Account accountWithInsufficientBalance =
                Account.create(
                        debitAccountId,
                        CustomerId.generate(),
                        Currency.of("USD"),
                        now
                );

        accountWithInsufficientBalance.credit(
                Money.of(
                        new BigDecimal("50.00"),
                        Currency.of("USD")
                )
        );

        when(accountRepository.findById(debitAccountId))
                .thenReturn(
                        Optional.of(accountWithInsufficientBalance)
                );

        when(accountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(creditAccount));

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

        verify(transactionRepository, never())
                .save(any(Transaction.class));

        verify(accountRepository, never())
                .save(creditAccount);
    }

    @Test
    void shouldDebitSourceAccount() {

        when(accountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(debitAccount));

        when(accountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(creditAccount));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.execute(
                debitAccountId,
                debitMoney,
                creditAccountId,
                usdToBrl
        );

        assertEquals(
                0,
                debitAccount.balance()
                        .amount()
                        .compareTo(new BigDecimal("900.00"))
        );

        verify(accountRepository)
                .save(debitAccount);
    }

    @Test
    void shouldCreditTargetAccount() {

        when(accountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(debitAccount));

        when(accountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(creditAccount));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.execute(
                debitAccountId,
                debitMoney,
                creditAccountId,
                usdToBrl
        );

        assertEquals(
                0,
                creditAccount.balance()
                        .amount()
                        .compareTo(new BigDecimal("540.00"))
        );

        verify(accountRepository)
                .save(creditAccount);
    }

}
