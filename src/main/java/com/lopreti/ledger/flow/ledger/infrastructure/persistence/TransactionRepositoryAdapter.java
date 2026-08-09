package com.lopreti.ledger.flow.ledger.infrastructure.persistence;

import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.ledger.application.port.out.TransactionRepository;
import com.lopreti.ledger.flow.ledger.domain.Posting;
import com.lopreti.ledger.flow.ledger.domain.Transaction;
import com.lopreti.ledger.flow.ledger.domain.TransactionId;
import com.lopreti.ledger.flow.shared.domain.Currency;
import com.lopreti.ledger.flow.shared.domain.ExchangeRate;
import com.lopreti.ledger.flow.shared.domain.Money;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TransactionRepositoryAdapter
        implements TransactionRepository {

    private final SpringDataTransactionRepository repository;

    public TransactionRepositoryAdapter(
            SpringDataTransactionRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        var exchangeRate = transaction.exchangeRate();

        TransactionJpaEntity entity =
                new TransactionJpaEntity(
                        transaction.id().value(),
                        transaction.baseCurrency().code(),
                        transaction.status(),
                        transaction.createdAt(),
                        exchangeRate != null
                                ? exchangeRate.from().code()
                                : null,
                        exchangeRate != null
                                ? exchangeRate.to().code()
                                : null,
                        exchangeRate != null
                                ? exchangeRate.rate()
                                : null
                );

        transaction.postings()
                .forEach(posting -> {

                    PostingJpaEntity postingEntity =
                            new PostingJpaEntity(
                                    posting.id().value(),
                                    posting.accountId().value(),
                                    posting.type(),
                                    posting.money()
                                            .currency()
                                            .code(),
                                    posting.money()
                                            .amount(),
                                    posting.functionalMoney()
                                            .currency()
                                            .code(),
                                    posting.functionalMoney()
                                            .amount(),
                                    posting.createdAt()
                            );

                    entity.addPosting(postingEntity);
                });

        repository.save(entity);

        return transaction;
    }

    private Transaction toDomain(
            TransactionJpaEntity entity
    ) {

        var postings = entity.getPostings()
                .stream()
                .map(this::toPosting)
                .toList();

        ExchangeRate exchangeRate = null;

        if (entity.getExchangeFromCurrency() != null
                && entity.getExchangeToCurrency() != null
                && entity.getExchangeRate() != null) {

            exchangeRate = new ExchangeRate(
                    Currency.of(entity.getExchangeFromCurrency()),
                    Currency.of(entity.getExchangeToCurrency()),
                    entity.getExchangeRate()
            );
        }

        return Transaction.reconstitute(
                TransactionId.of(entity.getId()),
                Currency.of(entity.getBaseCurrency()),
                entity.getCreatedAt(),
                entity.getStatus(),
                postings,
                exchangeRate
        );
    }

    @Override
    public Optional<Transaction> findById(
            TransactionId id
    ) {
        return repository.findById(id.value())
                .map(this::toDomain);
    }

    @Override
    public boolean existsById(
            TransactionId id
    ) {
        return repository.existsById(id.value());
    }

    @Override
    public List<Transaction> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Posting toPosting(
            PostingJpaEntity entity
    ) {

        Money money = Money.of(
                entity.getAmount(),
                Currency.of(entity.getCurrency())
        );

        Money functionalMoney = Money.of(
                entity.getBaseAmount(),
                Currency.of(entity.getBaseCurrency())
        );

        return Posting.reconstitute(
                com.lopreti.ledger.flow.ledger.domain.PostingId.of(
                        entity.getId()
                ),
                AccountId.of(entity.getAccountId()),
                entity.getType(),
                money,
                functionalMoney,
                entity.getCreatedAt()
        );
    }
}