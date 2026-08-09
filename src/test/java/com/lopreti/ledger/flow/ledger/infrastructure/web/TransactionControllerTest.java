package com.lopreti.ledger.flow.ledger.infrastructure.web;

import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.ledger.application.port.in.CreateDepositUseCase;
import com.lopreti.ledger.flow.ledger.application.port.in.CreateTransactionUseCase;
import com.lopreti.ledger.flow.ledger.domain.Posting;
import com.lopreti.ledger.flow.ledger.domain.PostingId;
import com.lopreti.ledger.flow.ledger.domain.Transaction;
import com.lopreti.ledger.flow.shared.domain.Currency;
import com.lopreti.ledger.flow.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateTransactionUseCase createTransactionUseCase;

    @MockitoBean
    private CreateDepositUseCase createDepositUseCase;

    @Test
    void shouldCreateTransaction() throws Exception {

        var debitAccountId = AccountId.generate();
        var creditAccountId = AccountId.generate();

        var transaction = Transaction.create(
                com.lopreti.ledger.flow.ledger.domain.TransactionId.generate(),
                Currency.of("BRL"),
                Instant.parse("2026-08-09T03:00:00Z")
        );

        transaction.addPosting(
                Posting.debit(
                        PostingId.generate(),
                        debitAccountId,
                        Money.of(
                                new BigDecimal("100.00"),
                                Currency.of("BRL")
                        ),
                        Money.of(
                                new BigDecimal("100.00"),
                                Currency.of("BRL")
                        ),
                        transaction.createdAt()
                )
        );

        transaction.addPosting(
                Posting.credit(
                        PostingId.generate(),
                        creditAccountId,
                        Money.of(
                                new BigDecimal("100.00"),
                                Currency.of("BRL")
                        ),
                        Money.of(
                                new BigDecimal("100.00"),
                                Currency.of("BRL")
                        ),
                        transaction.createdAt()
                )
        );

        transaction.post();

        when(createTransactionUseCase.execute(
                any(AccountId.class),
                any(Money.class),
                any(Money.class),
                any(AccountId.class),
                any(Money.class),
                any(Money.class)
        )).thenReturn(transaction);

        var request = """
                {
                    "debitAccountId": "%s",
                    "debitAmount": 100.00,
                    "debitFunctionalAmount": 100.00,
                    "debitCurrency": "BRL",
                    "debitFunctionalCurrency": "BRL",

                    "creditAccountId": "%s",
                    "creditAmount": 100.00,
                    "creditFunctionalAmount": 100.00,
                    "creditCurrency": "BRL",
                    "creditFunctionalCurrency": "BRL"
                }
                """.formatted(
                debitAccountId.value(),
                creditAccountId.value()
        );

        mockMvc.perform(
                        post("/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(jsonPath("$.id")
                        .value(transaction.id().value().toString()))
                .andExpect(jsonPath("$.baseCurrency")
                        .value("BRL"))
                .andExpect(jsonPath("$.status")
                        .value("POSTED"))
                .andExpect(jsonPath("$.postings.length()")
                        .value(2))

                .andExpect(jsonPath("$.postings[0].accountId")
                        .value(debitAccountId.value().toString()))
                .andExpect(jsonPath("$.postings[0].type")
                        .value("DEBIT"))
                .andExpect(jsonPath("$.postings[0].amount")
                        .value(100.00))
                .andExpect(jsonPath("$.postings[0].currency")
                        .value("BRL"))
                .andExpect(jsonPath("$.postings[0].functionalAmount")
                        .value(100.00))
                .andExpect(jsonPath("$.postings[0].functionalCurrency")
                        .value("BRL"))

                .andExpect(jsonPath("$.postings[1].accountId")
                        .value(creditAccountId.value().toString()))
                .andExpect(jsonPath("$.postings[1].type")
                        .value("CREDIT"))
                .andExpect(jsonPath("$.postings[1].amount")
                        .value(100.00))
                .andExpect(jsonPath("$.postings[1].currency")
                        .value("BRL"))
                .andExpect(jsonPath("$.postings[1].functionalAmount")
                        .value(100.00))
                .andExpect(jsonPath("$.postings[1].functionalCurrency")
                        .value("BRL"));

        verify(createTransactionUseCase)
                .execute(
                        any(AccountId.class),
                        any(Money.class),
                        any(Money.class),
                        any(AccountId.class),
                        any(Money.class),
                        any(Money.class)
                );
    }
}
