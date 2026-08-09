package com.lopreti.ledger.flow.ledger.application.service;

import com.lopreti.ledger.flow.account.application.port.out.AccountRepository;
import com.lopreti.ledger.flow.account.domain.Account;
import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.account.domain.CustomerId;
import com.lopreti.ledger.flow.ledger.application.port.out.TransactionRepository;
import com.lopreti.ledger.flow.ledger.domain.PostingType;
import com.lopreti.ledger.flow.ledger.domain.Transaction;
import com.lopreti.ledger.flow.ledger.domain.TransactionStatus;
import com.lopreti.ledger.flow.shared.domain.Currency;
import com.lopreti.ledger.flow.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    private Clock clock;

    private CreateTransactionService service;

    private final Instant fixedInstant =
            Instant.parse("2026-08-09T03:00:00Z");

    private Currency brl;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                fixedInstant,
                ZoneOffset.UTC
        );

        service = new CreateTransactionService(
                transactionRepository,
                accountRepository,
                clock
        );

        brl = Currency.of("BRL");
    }

    @Test
    void shouldCreateBalancedTransaction() {

        var debitAccount = createAccount("100.00");
        var creditAccount = createAccount("0.00");

        mockAccounts(
                debitAccount,
                creditAccount
        );

        var debitMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var creditMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var debitFunctionalMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var creditFunctionalMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        var result = service.execute(
                debitAccount.id(),
                debitMoney,
                debitFunctionalMoney,
                creditAccount.id(),
                creditMoney,
                creditFunctionalMoney
        );

        assertNotNull(result);

        assertNotNull(result.id());

        assertEquals(
                brl,
                result.baseCurrency()
        );

        assertEquals(
                fixedInstant,
                result.createdAt()
        );

        assertEquals(
                TransactionStatus.POSTED,
                result.status()
        );

        assertEquals(
                2,
                result.postings().size()
        );

        assertEquals(
                new BigDecimal("0.0000"),
                debitAccount.balance().amount()
        );

        assertEquals(
                new BigDecimal("100.0000"),
                creditAccount.balance().amount()
        );

        verify(transactionRepository)
                .save(result);
    }

    @Test
    void shouldCreateDebitAndCreditPostings() {

        var debitAccount = createAccount("100.00");
        var creditAccount = createAccount("0.00");

        mockAccounts(
                debitAccount,
                creditAccount
        );

        var debitMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var creditMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var debitFunctionalMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var creditFunctionalMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        var result = service.execute(
                debitAccount.id(),
                debitMoney,
                debitFunctionalMoney,
                creditAccount.id(),
                creditMoney,
                creditFunctionalMoney
        );

        var debit = result.postings()
                .stream()
                .filter(posting ->
                        posting.type() == PostingType.DEBIT)
                .findFirst()
                .orElseThrow();

        var credit = result.postings()
                .stream()
                .filter(posting ->
                        posting.type() == PostingType.CREDIT)
                .findFirst()
                .orElseThrow();

        assertEquals(
                debitAccount.id(),
                debit.accountId()
        );

        assertEquals(
                debitMoney,
                debit.money()
        );

        assertEquals(
                debitFunctionalMoney,
                debit.functionalMoney()
        );

        assertEquals(
                creditAccount.id(),
                credit.accountId()
        );

        assertEquals(
                creditMoney,
                credit.money()
        );

        assertEquals(
                creditFunctionalMoney,
                credit.functionalMoney()
        );

        assertEquals(
                fixedInstant,
                debit.createdAt()
        );

        assertEquals(
                fixedInstant,
                credit.createdAt()
        );
    }

    @Test
    void shouldGenerateDifferentTransactionIds() {

        var debitAccount = createAccount("200.00");
        var creditAccount = createAccount("0.00");

        mockAccounts(
                debitAccount,
                creditAccount
        );

        var money = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var functionalMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        var first = service.execute(
                debitAccount.id(),
                money,
                functionalMoney,
                creditAccount.id(),
                money,
                functionalMoney
        );

        var second = service.execute(
                debitAccount.id(),
                money,
                functionalMoney,
                creditAccount.id(),
                money,
                functionalMoney
        );

        assertNotEquals(
                first.id(),
                second.id()
        );

        assertEquals(
                new BigDecimal("0.0000"),
                debitAccount.balance().amount()
        );

        assertEquals(
                new BigDecimal("200.0000"),
                creditAccount.balance().amount()
        );

        verify(
                transactionRepository,
                times(2)
        ).save(any(Transaction.class));
    }

    @Test
    void shouldNotSaveUnbalancedTransaction() {

        var debitAccount = createAccount("100.00");
        var creditAccount = createAccount("0.00");

        mockAccounts(
                debitAccount,
                creditAccount
        );

        var debitMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var creditMoney = Money.of(
                new BigDecimal("90.00"),
                brl
        );

        var debitFunctionalMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var creditFunctionalMoney = Money.of(
                new BigDecimal("90.00"),
                brl
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.execute(
                        debitAccount.id(),
                        debitMoney,
                        debitFunctionalMoney,
                        creditAccount.id(),
                        creditMoney,
                        creditFunctionalMoney
                )
        );

        verify(
                transactionRepository,
                never()
        ).save(any(Transaction.class));
    }

    @Test
    void shouldSaveCreatedTransaction() {

        var debitAccount = createAccount("100.00");
        var creditAccount = createAccount("0.00");

        mockAccounts(
                debitAccount,
                creditAccount
        );

        var money = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        service.execute(
                debitAccount.id(),
                money,
                money,
                creditAccount.id(),
                money,
                money
        );

        ArgumentCaptor<Transaction> captor =
                ArgumentCaptor.forClass(Transaction.class);

        verify(transactionRepository)
                .save(captor.capture());

        var savedTransaction = captor.getValue();

        assertNotNull(savedTransaction);

        assertEquals(
                TransactionStatus.POSTED,
                savedTransaction.status()
        );

        assertEquals(
                2,
                savedTransaction.postings().size()
        );
    }

    private Account createAccount(
            String initialBalance
    ) {
        var account = Account.create(
                AccountId.generate(),
                CustomerId.of(UUID.randomUUID()),
                brl,
                fixedInstant
        );

        if (new BigDecimal(initialBalance)
                .compareTo(BigDecimal.ZERO) > 0) {

            account.credit(
                    Money.of(
                            new BigDecimal(initialBalance),
                            brl
                    )
            );
        }

        return account;
    }

    private void mockAccounts(
            Account debitAccount,
            Account creditAccount
    ) {
        when(accountRepository.findById(debitAccount.id()))
                .thenReturn(Optional.of(debitAccount));

        when(accountRepository.findById(creditAccount.id()))
                .thenReturn(Optional.of(creditAccount));
    }
}
