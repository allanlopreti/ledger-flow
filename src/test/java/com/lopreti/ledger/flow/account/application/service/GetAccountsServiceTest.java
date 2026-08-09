package com.lopreti.ledger.flow.account.application.service;

import com.lopreti.ledger.flow.account.application.port.out.AccountRepository;
import com.lopreti.ledger.flow.account.domain.Account;
import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.account.domain.CustomerId;
import com.lopreti.ledger.flow.shared.domain.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAccountsServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private GetAccountsService service;

    @Test
    void shouldReturnAllAccounts() {
        Account account1 = Account.create(
                AccountId.generate(),
                CustomerId.generate(),
                Currency.of("BRL"),
                Instant.now()
        );

        Account account2 = Account.create(
                AccountId.generate(),
                CustomerId.generate(),
                Currency.of("USD"),
                Instant.now()
        );

        when(accountRepository.findAll())
                .thenReturn(List.of(account1, account2));

        var result = service.execute();

        assertEquals(2, result.size());
        assertEquals(account1.id(), result.get(0).id());
        assertEquals(account2.id(), result.get(1).id());
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoAccounts() {
        when(accountRepository.findAll())
                .thenReturn(List.of());

        var result = service.execute();

        assertEquals(0, result.size());
    }
}