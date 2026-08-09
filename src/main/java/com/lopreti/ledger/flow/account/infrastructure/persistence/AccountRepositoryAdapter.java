package com.lopreti.ledger.flow.account.infrastructure.persistence;

import com.lopreti.ledger.flow.account.application.port.out.AccountRepository;
import com.lopreti.ledger.flow.account.domain.Account;
import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.account.domain.CustomerId;
import com.lopreti.ledger.flow.shared.domain.Currency;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AccountRepositoryAdapter implements AccountRepository {

    private final SpringDataAccountRepository repository;

    public AccountRepositoryAdapter(
            SpringDataAccountRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Account save(Account account) {

        AccountJpaEntity entity = new AccountJpaEntity(
                account.id().value(),
                account.customerId().value(),
                account.currency().code(),
                account.status(),
                account.createdAt()
        );

        repository.save(entity);

        return account;
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return repository.findById(id.value())
                .map(this::toDomain);
    }

    @Override
    public boolean existsById(AccountId id) {
        return repository.existsById(id.value());
    }

    private Account toDomain(AccountJpaEntity entity) {

        return Account.reconstitute(
                AccountId.of(entity.getId()),
                CustomerId.of(entity.getCustomerId()),
                Currency.of(entity.getCurrency()),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}