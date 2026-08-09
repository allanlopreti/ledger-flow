package com.lopreti.ledger.flow.ledger.infrastructure.persistence;

import com.lopreti.ledger.flow.ledger.domain.TransactionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionJpaEntity {

    @Id
    private UUID id;

    @Column(
            name = "base_currency",
            nullable = false,
            length = 3
    )
    private String baseCurrency;

    @Column(
            name = "exchange_from_currency",
            length = 3
    )
    private String exchangeFromCurrency;

    @Column(
            name = "exchange_to_currency",
            length = 3
    )
    private String exchangeToCurrency;

    @Column(
            name = "exchange_rate",
            precision = 19,
            scale = 8
    )
    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false
    )
    private TransactionStatus status;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    @OneToMany(
            mappedBy = "transaction",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PostingJpaEntity> postings = new ArrayList<>();

    protected TransactionJpaEntity() {
    }

    public TransactionJpaEntity(
            UUID id,
            String baseCurrency,
            TransactionStatus status,
            Instant createdAt
    ) {
        this(
                id,
                baseCurrency,
                status,
                createdAt,
                null,
                null,
                null
        );
    }

    public TransactionJpaEntity(
            UUID id,
            String baseCurrency,
            TransactionStatus status,
            Instant createdAt,
            String exchangeFromCurrency,
            String exchangeToCurrency,
            BigDecimal exchangeRate
    ) {
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.status = status;
        this.createdAt = createdAt;
        this.exchangeFromCurrency = exchangeFromCurrency;
        this.exchangeToCurrency = exchangeToCurrency;
        this.exchangeRate = exchangeRate;
    }

    public void addPosting(PostingJpaEntity posting) {
        postings.add(posting);
        posting.setTransaction(this);
    }

    public UUID getId() {
        return id;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getExchangeFromCurrency() {
        return exchangeFromCurrency;
    }

    public String getExchangeToCurrency() {
        return exchangeToCurrency;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<PostingJpaEntity> getPostings() {
        return postings;
    }
}