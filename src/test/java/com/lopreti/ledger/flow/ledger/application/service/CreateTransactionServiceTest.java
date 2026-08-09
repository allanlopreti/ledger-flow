package com.lopreti.ledger.flow.ledger.application.service;

import com.lopreti.ledger.flow.account.domain.AccountId;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

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
                clock
        );

        brl = Currency.of("BRL");
    }

    @Test
    void shouldCreateBalancedTransaction() {
        var debitAccountId = AccountId.of(
                UUID.randomUUID()
        );

        var creditAccountId = AccountId.of(
                UUID.randomUUID()
        );

        var debitMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var creditMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var debitBaseMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var creditBaseMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        var result = service.execute(
                debitAccountId,
                debitMoney,
                debitBaseMoney,
                creditAccountId,
                creditMoney,
                creditBaseMoney
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

        verify(transactionRepository)
                .save(result);
    }

    @Test
    void shouldCreateDebitAndCreditPostings() {
        var debitAccountId = AccountId.of(
                UUID.randomUUID()
        );

        var creditAccountId = AccountId.of(
                UUID.randomUUID()
        );

        var debitMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var creditMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var debitBaseMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var creditBaseMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        var result = service.execute(
                debitAccountId,
                debitMoney,
                debitBaseMoney,
                creditAccountId,
                creditMoney,
                creditBaseMoney
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
                debitAccountId,
                debit.accountId()
        );

        assertEquals(
                debitMoney,
                debit.money()
        );

        assertEquals(
                debitBaseMoney,
                debit.baseMoney()
        );

        assertEquals(
                creditAccountId,
                credit.accountId()
        );

        assertEquals(
                creditMoney,
                credit.money()
        );

        assertEquals(
                creditBaseMoney,
                credit.baseMoney()
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
        var debitAccountId = AccountId.generate();
        var creditAccountId = AccountId.generate();

        var money = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var baseMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        var first = service.execute(
                debitAccountId,
                money,
                baseMoney,
                creditAccountId,
                money,
                baseMoney
        );

        var second = service.execute(
                debitAccountId,
                money,
                baseMoney,
                creditAccountId,
                money,
                baseMoney
        );

        assertNotEquals(
                first.id(),
                second.id()
        );

        verify(transactionRepository, times(2))
                .save(any(Transaction.class));
    }

    @Test
    void shouldNotSaveUnbalancedTransaction() {
        var debitAccountId = AccountId.generate();
        var creditAccountId = AccountId.generate();

        var debitMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var creditMoney = Money.of(
                new BigDecimal("90.00"),
                brl
        );

        var debitBaseMoney = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        var creditBaseMoney = Money.of(
                new BigDecimal("90.00"),
                brl
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.execute(
                        debitAccountId,
                        debitMoney,
                        debitBaseMoney,
                        creditAccountId,
                        creditMoney,
                        creditBaseMoney
                )
        );

        verify(
                transactionRepository,
                never()
        ).save(any(Transaction.class));
    }

    @Test
    void shouldSaveCreatedTransaction() {
        var debitAccountId = AccountId.generate();
        var creditAccountId = AccountId.generate();

        var money = Money.of(
                new BigDecimal("100.00"),
                brl
        );

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        service.execute(
                debitAccountId,
                money,
                money,
                creditAccountId,
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
}
