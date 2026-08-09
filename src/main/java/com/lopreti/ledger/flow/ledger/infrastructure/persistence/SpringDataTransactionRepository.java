package com.lopreti.ledger.flow.ledger.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataTransactionRepository
        extends JpaRepository<TransactionJpaEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = "postings")
    Optional<TransactionJpaEntity> findById(UUID id);
}