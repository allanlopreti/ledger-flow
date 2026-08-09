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
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.status = status;
        this.createdAt = createdAt;
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