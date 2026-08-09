package com.lopreti.ledger.flow.ledger.infrastructure.web;

import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.account.infrastructure.web.dto.AccountBalanceResponse;
import com.lopreti.ledger.flow.ledger.application.port.in.CreateDepositUseCase;
import com.lopreti.ledger.flow.ledger.application.port.in.CreateFxTransactionUseCase;
import com.lopreti.ledger.flow.ledger.application.port.in.CreateTransactionUseCase;
import com.lopreti.ledger.flow.ledger.application.port.in.GetTransactionHistoryUseCase;
import com.lopreti.ledger.flow.ledger.domain.Transaction;
import com.lopreti.ledger.flow.ledger.infrastructure.web.dto.CreateDepositRequest;
import com.lopreti.ledger.flow.ledger.infrastructure.web.dto.CreateFxTransactionRequest;
import com.lopreti.ledger.flow.ledger.infrastructure.web.dto.CreateTransactionRequest;
import com.lopreti.ledger.flow.ledger.infrastructure.web.dto.TransactionHistoryResponse;
import com.lopreti.ledger.flow.ledger.infrastructure.web.dto.TransactionResponse;
import com.lopreti.ledger.flow.shared.domain.Currency;
import com.lopreti.ledger.flow.shared.domain.ExchangeRate;
import com.lopreti.ledger.flow.shared.domain.Money;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final CreateDepositUseCase createDepositUseCase;
    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;
    private final CreateFxTransactionUseCase createFxTransactionUseCase;

    public TransactionController(
            CreateTransactionUseCase createTransactionUseCase,
            CreateDepositUseCase createDepositUseCase,
            GetTransactionHistoryUseCase getTransactionHistoryUseCase,
            CreateFxTransactionUseCase createFxTransactionUseCase
    ) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.createDepositUseCase = createDepositUseCase;
        this.getTransactionHistoryUseCase = getTransactionHistoryUseCase;
        this.createFxTransactionUseCase = createFxTransactionUseCase;
    }

    @GetMapping
    public ResponseEntity<List<TransactionHistoryResponse>> history() {

        var transactions =
                getTransactionHistoryUseCase.execute()
                        .stream()
                        .map(TransactionHistoryResponse::from)
                        .toList();

        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody CreateTransactionRequest request
    ) {

        var debitMoney = Money.of(
                request.debitAmount(),
                Currency.of(request.debitCurrency())
        );

        var debitFunctionalMoney = Money.of(
                request.debitFunctionalAmount(),
                Currency.of(request.debitFunctionalCurrency())
        );

        var creditMoney = Money.of(
                request.creditAmount(),
                Currency.of(request.creditCurrency())
        );

        var creditFunctionalMoney = Money.of(
                request.creditFunctionalAmount(),
                Currency.of(request.creditFunctionalCurrency())
        );

        Transaction transaction =
                createTransactionUseCase.execute(
                        AccountId.of(request.debitAccountId()),
                        debitMoney,
                        debitFunctionalMoney,
                        AccountId.of(request.creditAccountId()),
                        creditMoney,
                        creditFunctionalMoney
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TransactionResponse.from(transaction));
    }

    @PostMapping("/deposit")
    public ResponseEntity<AccountBalanceResponse> deposit(
            @Valid @RequestBody CreateDepositRequest request
    ) {

        var balance =
                createDepositUseCase.execute(
                        AccountId.of(request.accountId()),
                        request.amount(),
                        Currency.of(request.currency())
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        AccountBalanceResponse.from(balance)
                );
    }

    @PostMapping("/fx")
    public ResponseEntity<TransactionResponse> createFxTransaction(
            @RequestBody CreateFxTransactionRequest request
    ) {

        Money debitMoney = Money.of(
                request.debitAmount(),
                Currency.of(request.debitCurrency())
        );

        ExchangeRate exchangeRate =
                new ExchangeRate(
                        Currency.of(request.debitCurrency()),
                        Currency.of(request.creditCurrency()),
                        request.exchangeRate()
                );

        Transaction transaction =
                createFxTransactionUseCase.execute(
                        AccountId.of(request.debitAccountId()),
                        debitMoney,
                        AccountId.of(request.creditAccountId()),
                        exchangeRate
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TransactionResponse.from(transaction));
    }
}