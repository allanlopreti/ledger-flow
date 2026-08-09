package com.lopreti.ledger.flow.ledger.infrastructure.persistence;

import com.lopreti.ledger.flow.ledger.domain.PostingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "postings")
public class PostingJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "transaction_id",
            nullable = false
    )
    private TransactionJpaEntity transaction;

    @Column(
            name = "account_id",
            nullable = false
    )
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "type",
            nullable = false
    )
    private PostingType type;

    @Column(
            name = "currency",
            nullable = false,
            length = 3
    )
    private String currency;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 6
    )
    private BigDecimal amount;

    @Column(
            name = "base_currency",
            nullable = false,
            length = 3
    )
    private String baseCurrency;

    @Column(
            name = "base_amount",
            nullable = false,
            precision = 19,
            scale = 6
    )
    private BigDecimal baseAmount;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    protected PostingJpaEntity() {
    }

    public PostingJpaEntity(
            UUID id,
            UUID accountId,
            PostingType type,
            String currency,
            BigDecimal amount,
            String baseCurrency,
            BigDecimal baseAmount,
            Instant createdAt
    ) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.currency = currency;
        this.amount = amount;
        this.baseCurrency = baseCurrency;
        this.baseAmount = baseAmount;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public PostingType getType() {
        return type;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public TransactionJpaEntity getTransaction() {
        return transaction;
    }

    public void setTransaction(
            TransactionJpaEntity transaction
    ) {
        this.transaction = transaction;
    }
}