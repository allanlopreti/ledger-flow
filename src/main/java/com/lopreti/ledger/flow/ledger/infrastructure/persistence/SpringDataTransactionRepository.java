package com.lopreti.ledger.flow.ledger.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataTransactionRepository
        extends JpaRepository<TransactionJpaEntity, UUID> {
}