package com.lopreti.ledger.flow.ledger.infrastructure.persistence;

import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.account.domain.AccountStatus;
import com.lopreti.ledger.flow.account.infrastructure.persistence.AccountJpaEntity;
import com.lopreti.ledger.flow.account.infrastructure.persistence.SpringDataAccountRepository;
import com.lopreti.ledger.flow.ledger.domain.Posting;
import com.lopreti.ledger.flow.ledger.domain.PostingId;
import com.lopreti.ledger.flow.ledger.domain.PostingType;
import com.lopreti.ledger.flow.ledger.domain.Transaction;
import com.lopreti.ledger.flow.ledger.domain.TransactionId;
import com.lopreti.ledger.flow.ledger.domain.TransactionStatus;
import com.lopreti.ledger.flow.shared.domain.Currency;
import com.lopreti.ledger.flow.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class TransactionRepositoryAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private SpringDataAccountRepository accountRepository;

    @Autowired
    private TransactionRepositoryAdapter transactionRepository;

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
    void shouldSaveAndRetrieveTransaction() {

        var brl = Currency.of("BRL");

        var transactionId = TransactionId.generate();

        var debitAccountId = AccountId.generate();
        var creditAccountId = AccountId.generate();

        createAccount(debitAccountId);
        createAccount(creditAccountId);

        var now = Instant.now();

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

        var transaction = Transaction.create(
                transactionId,
                brl,
                now
        );

        var debitPostingId = PostingId.generate();
        var creditPostingId = PostingId.generate();

        transaction.addPosting(
                Posting.debit(
                        debitPostingId,
                        debitAccountId,
                        debitMoney,
                        debitBaseMoney,
                        now
                )
        );

        transaction.addPosting(
                Posting.credit(
                        creditPostingId,
                        creditAccountId,
                        creditMoney,
                        creditBaseMoney,
                        now
                )
        );

        transaction.post();

        transactionRepository.save(transaction);

        var result = transactionRepository.findById(
                transactionId
        );

        assertTrue(result.isPresent());

        var persisted = result.get();

        assertEquals(
                transactionId,
                persisted.id()
        );

        assertEquals(
                brl,
                persisted.baseCurrency()
        );

        assertEquals(
                TransactionStatus.POSTED,
                persisted.status()
        );

        assertEquals(
                2,
                persisted.postings().size()
        );

        var debit = persisted.postings()
                .stream()
                .filter(posting ->
                        posting.type() == PostingType.DEBIT)
                .findFirst()
                .orElseThrow();

        assertEquals(
                debitPostingId,
                debit.id()
        );

        assertEquals(
                debitAccountId,
                debit.accountId()
        );

        assertMoneyEquals(
                debitMoney,
                debit.money()
        );

        assertMoneyEquals(
                debitBaseMoney,
                debit.baseMoney()
        );

        var credit = persisted.postings()
                .stream()
                .filter(posting ->
                        posting.type() == PostingType.CREDIT)
                .findFirst()
                .orElseThrow();

        assertEquals(
                creditPostingId,
                credit.id()
        );

        assertEquals(
                creditAccountId,
                credit.accountId()
        );

        assertMoneyEquals(
                creditMoney,
                credit.money()
        );

        assertMoneyEquals(
                creditBaseMoney,
                credit.baseMoney()
        );
    }

    @Test
    void shouldReturnEmptyWhenTransactionDoesNotExist() {

        var result = transactionRepository.findById(
                TransactionId.generate()
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCheckIfTransactionExists() {

        var brl = Currency.of("BRL");

        var transactionId = TransactionId.generate();

        var debitAccountId = AccountId.generate();
        var creditAccountId = AccountId.generate();

        createAccount(debitAccountId);
        createAccount(creditAccountId);

        var transaction = Transaction.create(
                transactionId,
                brl,
                Instant.now()
        );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        debitAccountId,
                        Money.of(
                                new BigDecimal("100.00"),
                                brl
                        ),
                        Money.of(
                                new BigDecimal("100.00"),
                                brl
                        ),
                        Instant.now()
                )
        );

        transaction.addPosting(
                Posting.credit(
                        PostingId.generate(),
                        creditAccountId,
                        Money.of(
                                new BigDecimal("100.00"),
                                brl
                        ),
                        Money.of(
                                new BigDecimal("100.00"),
                                brl
                        ),
                        Instant.now()
                )
        );

        transaction.post();

        transactionRepository.save(transaction);

        assertTrue(
                transactionRepository.existsById(transactionId)
        );
    }

    private void assertMoneyEquals(
            Money expected,
            Money actual
    ) {
        assertEquals(
                expected.currency(),
                actual.currency()
        );

        assertEquals(
                0,
                expected.amount().compareTo(
                        actual.amount()
                )
        );
    }

    private void createAccount(
            AccountId accountId
    ) {
        accountRepository.save(
                new AccountJpaEntity(
                        accountId.value(),
                        UUID.randomUUID(),
                        "BRL",
                        AccountStatus.ACTIVE,
                        Instant.now()
                )
        );
    }
}
