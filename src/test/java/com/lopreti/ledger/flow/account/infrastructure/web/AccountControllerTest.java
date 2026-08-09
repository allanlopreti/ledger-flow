package com.lopreti.ledger.flow.account.infrastructure.web;

import com.lopreti.ledger.flow.account.application.port.in.CreateAccountUseCase;
import com.lopreti.ledger.flow.account.application.port.in.GetAccountBalanceUseCase;
import com.lopreti.ledger.flow.account.application.port.in.GetAccountsUseCase;
import com.lopreti.ledger.flow.account.domain.Account;
import com.lopreti.ledger.flow.account.domain.AccountBalance;
import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.account.domain.CustomerId;
import com.lopreti.ledger.flow.shared.domain.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateAccountUseCase createAccountUseCase;

    @MockitoBean
    private GetAccountBalanceUseCase getAccountBalanceUseCase;

    @MockitoBean
    private GetAccountsUseCase getAccountsUseCase;

    @Test
    void shouldCreateAccount() throws Exception {

        var customerId = UUID.randomUUID();
        var accountId = AccountId.generate();
        var createdAt = Instant.parse(
                "2026-08-09T03:00:00Z"
        );

        var account = Account.create(
                accountId,
                CustomerId.of(customerId),
                Currency.of("BRL"),
                createdAt
        );

        when(createAccountUseCase.execute(
                any(CustomerId.class),
                any(Currency.class)
        )).thenReturn(account);

        var request = """
                {
                    "customerId": "%s",
                    "currency": "BRL"
                }
                """.formatted(customerId);

        mockMvc.perform(
                        post("/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id")
                        .value(accountId.value().toString()))
                .andExpect(jsonPath("$.customerId")
                        .value(customerId.toString()))
                .andExpect(jsonPath("$.currency")
                        .value("BRL"))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt")
                        .value(createdAt.toString()));

        verify(createAccountUseCase)
                .execute(
                        any(CustomerId.class),
                        any(Currency.class)
                );
    }

    @Test
    void shouldGetAccountBalance() throws Exception {
        UUID accountId = UUID.randomUUID();

        AccountBalance balance =
                new AccountBalance(
                        new BigDecimal("150.0000"),
                        Currency.of("BRL")
                );

        when(getAccountBalanceUseCase.execute(
                AccountId.of(accountId)
        )).thenReturn(balance);

        mockMvc.perform(
                        get("/accounts/{accountId}/balance", accountId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150.0))
                .andExpect(jsonPath("$.currency").value("BRL"));

        verify(getAccountBalanceUseCase)
                .execute(AccountId.of(accountId));
    }

    @Test
    void shouldRejectRequestWithoutCustomerId()
            throws Exception {

        var request = """
                {
                    "currency": "BRL"
                }
                """;

        mockMvc.perform(
                        post("/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createAccountUseCase);
    }

    @Test
    void shouldRejectRequestWithoutCurrency()
            throws Exception {

        var customerId = UUID.randomUUID();

        var request = """
                {
                    "customerId": "%s"
                }
                """.formatted(customerId);

        mockMvc.perform(
                        post("/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createAccountUseCase);
    }

    @Test
    void shouldRejectInvalidCustomerId()
            throws Exception {

        var request = """
                {
                    "customerId": "invalid-uuid",
                    "currency": "BRL"
                }
                """;

        mockMvc.perform(
                        post("/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createAccountUseCase);
    }

    @Test
    void shouldRejectInvalidJson()
            throws Exception {

        var request = """
                {
                    "customerId": "invalid-json",
                    "currency": "BRL"
                """;

        mockMvc.perform(
                        post("/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createAccountUseCase);
    }

    @Test
    void shouldGetAllAccounts() throws Exception {
        Account brlAccount = Account.create(
                AccountId.generate(),
                CustomerId.generate(),
                Currency.of("BRL"),
                Instant.parse("2026-08-09T04:00:00Z")
        );

        Account usdAccount = Account.create(
                AccountId.generate(),
                CustomerId.generate(),
                Currency.of("USD"),
                Instant.parse("2026-08-09T04:01:00Z")
        );

        when(getAccountsUseCase.execute())
                .thenReturn(List.of(
                        brlAccount,
                        usdAccount
                ));

        mockMvc.perform(
                        get("/accounts")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].id")
                        .value(brlAccount.id().value().toString()))
                .andExpect(jsonPath("$[0].customerId")
                        .value(brlAccount.customerId().value().toString()))
                .andExpect(jsonPath("$[0].currency")
                        .value("BRL"))
                .andExpect(jsonPath("$[0].status")
                        .value("ACTIVE"))

                .andExpect(jsonPath("$[1].id")
                        .value(usdAccount.id().value().toString()))
                .andExpect(jsonPath("$[1].customerId")
                        .value(usdAccount.customerId().value().toString()))
                .andExpect(jsonPath("$[1].currency")
                        .value("USD"))
                .andExpect(jsonPath("$[1].status")
                        .value("ACTIVE"));
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoAccounts()
            throws Exception {

        when(getAccountsUseCase.execute())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/accounts")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
