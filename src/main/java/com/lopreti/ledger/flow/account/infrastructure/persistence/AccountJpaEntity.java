package com.lopreti.ledger.flow.account.infrastructure.persistence;

import com.lopreti.ledger.flow.account.domain.AccountStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "accounts",
        indexes = {
                @Index(
                        name = "idx_accounts_customer_id",
                        columnList = "customer_id"
                )
        }
)
public class AccountJpaEntity {

    @Id
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    private UUID id;

    @Column(
            name = "customer_id",
            nullable = false,
            updatable = false
    )
    private UUID customerId;

    @Column(
            name = "currency",
            nullable = false,
            length = 3,
            updatable = false
    )
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private AccountStatus status;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected AccountJpaEntity() {
    }

    public AccountJpaEntity(
            UUID id,
            UUID customerId,
            String currency,
            AccountStatus status,
            Instant createdAt
    ) {
        this.id = id;
        this.customerId = customerId;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getCurrency() {
        return currency;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}